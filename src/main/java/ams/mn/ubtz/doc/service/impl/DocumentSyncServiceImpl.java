package ams.mn.ubtz.doc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import ams.mn.ubtz.doc.service.DocumentSyncService;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import ams.mn.ubtz.common.dto.SyncResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DocumentSyncServiceImpl implements DocumentSyncService {

	@Autowired
    private RestTemplate restTemplate;

    @Value("${remote.api.url}")
    private String remoteApiUrl;
    
    @Value("${file.download.base.url}")
    private String fileBaseUrl;

    @Value("${file.save.directory}")
    private String saveDirectory;

    @Value("${download.thread.pool.size:10}")
    private int threadPoolSize;

    @Value("${download.max.retry:3}")
    private int maxRetry;

    @Autowired
    private DataSource dataSource;

    // 1. Санах ойд cache хийх Set
    private final Set<Integer> cachedNpids = new HashSet<>();
    
    private static final Logger log = LoggerFactory.getLogger(DocumentSyncServiceImpl.class);

    @Override
    public SyncResult syncAllFromRemote() {
        int page = 0;
        int size = 1000;

        int totalInserted = 0;
        int totalUpdated = 0;

        while (true) {
            String url = remoteApiUrl + "/list?page=" + page + "&size=" + size;

            List<Map<String, Object>> dataList;
            try {
                dataList = restTemplate.getForObject(url, List.class);
            } catch (Exception e) {
                System.err.println("Алдаа: " + e.getMessage());
                break;
            }

            if (dataList == null || dataList.isEmpty()) {
                break;
            }

            SyncResult batchResult = saveOrUpdateToLocalDatabase(dataList);
            totalInserted += batchResult.getInserted();
            totalUpdated += batchResult.getUpdated();

            System.out.println("Page " + page + ": " + batchResult.getInserted() + " insert, " + batchResult.getUpdated() + " update");
            page++;
        }

        return new SyncResult(totalInserted, totalUpdated);
    }

    /*public void syncAllFromRemote() {
        // 2. Анх нэг удаа байгаа npid-уудыг ачааллана
        //loadExistingNpids();
        int page = 0;
        int size = 1000;

        while (true) {
            String url = remoteApiUrl + "/list?page=" + page + "&size=" + size;

            List<Map<String, Object>> dataList;
            try {
                dataList = restTemplate.getForObject(url, List.class);
            } catch (Exception e) {
                System.err.println("Алдаа: " + e.getMessage());
                break;
            }

            if (dataList.isEmpty()) {
                System.out.println("Датаны төгсгөлд хүрлээ. Нийт амжилттай татсан хуудсууд: " + page);
                break;
            }
            
            //if(cachedNpids.size()==0) 
            	//saveToLocalDatabase(dataList);
           // else 
            //	saveOrUpdateToLocalDatabase(dataList);
            	

            //saveToLocalDatabase(dataList);
            saveOrUpdateToLocalDatabase(dataList);
            //saveNewOnlyToLocalDatabase(dataList); //exist
            
            System.out.println("Page " + page + ": " + dataList.size() + " мөр хадгалагдлаа");

            page++;
        }
    }*/
    
    
    
    private SyncResult saveOrUpdateToLocalDatabase(List<Map<String, Object>> dataList) {
        String sql =
            "MERGE INTO a_document AS target " +
            "USING (SELECT ? AS npid, ? AS pid, ? AS t1, ? AS t2, ? AS t3, ? AS t4, ? AS t5, ? AS t6, ? AS t7, ? AS t8, ? AS t9) AS source " +
            "ON target.npid = source.npid " +
            "WHEN MATCHED AND (" +
            "    ISNULL(target.pid, '') <> ISNULL(source.pid, '') OR " +
            "    ISNULL(target.t1, '') <> ISNULL(source.t1, '') OR " +
            "    ISNULL(target.t2, '') <> ISNULL(source.t2, '') OR " +
            "    ISNULL(target.t3, '') <> ISNULL(source.t3, '') OR " +
            "    ISNULL(target.t4, '') <> ISNULL(source.t4, '') OR " +
            "    ISNULL(target.t5, '') <> ISNULL(source.t5, '') OR " +
            "    ISNULL(target.t6, '') <> ISNULL(source.t6, '') OR " +
            "    ISNULL(CAST(target.t7 AS VARCHAR), '') <> ISNULL(CAST(source.t7 AS VARCHAR), '') OR " +
            "    ISNULL(CAST(target.t8 AS VARCHAR), '') <> ISNULL(CAST(source.t8 AS VARCHAR), '') OR " +
            "    ISNULL(target.t9, '') <> ISNULL(source.t9, '')" +
            ") THEN UPDATE SET " +
            "    pid = source.pid, t1 = source.t1, t2 = source.t2, t3 = source.t3, t4 = source.t4, " +
            "    t5 = source.t5, t6 = source.t6, t7 = source.t7, t8 = source.t8, t9 = source.t9 " +
            "WHEN NOT MATCHED THEN " +
            "INSERT (npid, pid, t1, t2, t3, t4, t5, t6, t7, t8, t9) " +
            "VALUES (source.npid, source.pid, source.t1, source.t2, source.t3, source.t4, source.t5, source.t6, source.t7, source.t8, source.t9) " +
            "OUTPUT $action AS Action;";

        int inserted = 0;
        int updated = 0;

        try (Connection conn = dataSource.getConnection()) {
            for (Map<String, Object> row : dataList) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, (Integer) row.get("npid"));
                    stmt.setString(2, (String) row.get("pid"));
                    stmt.setString(3, (String) row.get("t1"));
                    stmt.setDate(4, parseSqlDate(row.get("t2")));
                    stmt.setString(5, (String) row.get("t3"));
                    stmt.setString(6, (String) row.get("t4"));
                    stmt.setString(7, (String) row.get("t5"));
                    stmt.setString(8, (String) row.get("t6"));
                    stmt.setObject(9, row.get("t7"));
                    stmt.setTimestamp(10, parseSqlTimestamp(row.get("t8")));
                    stmt.setString(11, (String) row.get("t9"));

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            String action = rs.getString("Action");
                            if ("INSERT".equalsIgnoreCase(action)) inserted++;
                            else if ("UPDATE".equalsIgnoreCase(action)) updated++;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("MERGE хийхэд алдаа: " + e.getMessage());
        }

        return new SyncResult(inserted, updated);
    }

    
    // Анх байгаа npid-уудыг cache хийх
    private void loadExistingNpids() {
    	
        String sql = "SELECT npid FROM a_document";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                cachedNpids.add(rs.getInt("npid"));
            }
        } catch (SQLException e) {
            System.err.println("Npid татахад алдаа гарлаа: " + e.getMessage());
        }
        
        System.out.println("cashe duudav мөр амжилттай нэмэгдлээ.");
    }
    
    /*private void saveNewOnlyToLocalDatabase(List<Map<String, Object>> dataList) {
        String sql =
            "INSERT INTO a_document (npid, pid, t1, t2, t3, t4, t5, t6, t7, t8, t9) " +
            "SELECT ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? " +
            "WHERE NOT EXISTS (SELECT 1 FROM a_document WHERE npid = ?);";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Map<String, Object> row : dataList) {
                try {
                    int npid = (Integer) row.get("npid");

                    stmt.setInt(1, npid);
                    stmt.setString(2, (String) row.get("pid"));
                    stmt.setString(3, (String) row.get("t1"));
                    stmt.setDate(4, parseSqlDate(row.get("t2")));
                    stmt.setString(5, (String) row.get("t3"));
                    stmt.setString(6, (String) row.get("t4"));
                    stmt.setString(7, (String) row.get("t5"));
                    stmt.setString(8, (String) row.get("t6"));
                    stmt.setObject(9, row.get("t7"));
                    stmt.setTimestamp(10, parseSqlTimestamp(row.get("t8")));
                    stmt.setString(11, (String) row.get("t9"));

                    stmt.setInt(12, npid); // WHERE NOT EXISTS-д дахин npid
                    //stmt.setInt(13, npid); // WHERE NOT EXISTS-д дахин npid

                    stmt.addBatch();
                } catch (Exception e) {
                    System.err.println("Мөр боловсруулахад алдаа: " + row + " -> " + e.getMessage());
                }
            }

            stmt.executeBatch();

        } catch (SQLException e) {
            System.err.println("Batch INSERT хийхэд алдаа: " + e.getMessage());
        }
    }*/

    
    

   

    private void saveToLocalDatabase2(List<Map<String, Object>> dataList) {
        
    	String sql = "INSERT INTO a_document (npid, pid, t1, t2, t3, t4, t5, t6, t7, t8, t9) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // 2. Зөвхөн шинэ npid-тай мөрүүдийг ялгаж авах
        List<Map<String, Object>> newRows = new ArrayList<>();
        for (Map<String, Object> row : dataList) {
            Integer npid = (Integer) row.get("npid"); // npid баталгаатай int гэж үзэж байна
            if (cachedNpids.add(npid)) { // add() нь тухайн npid шинэ бол true өгнө
                newRows.add(row);
            }
        }

        // 3. Шинэ мөрүүдийг batch insert хийх
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Map<String, Object> row : newRows) {
                try {
                    stmt.setInt(1, (Integer) row.get("npid"));
                    stmt.setString(2, (String) row.get("pid"));
                    stmt.setString(3, (String) row.get("t1"));
                    stmt.setDate(4, parseSqlDate(row.get("t2")));
                    stmt.setString(5, (String) row.get("t3"));
                    stmt.setString(6, (String) row.get("t4"));
                    stmt.setString(7, (String) row.get("t5"));
                    stmt.setString(8, (String) row.get("t6"));
                    stmt.setInt(9, (Integer) row.get("t7"));
                    stmt.setTimestamp(10, parseSqlTimestamp(row.get("t8")));
                    stmt.setString(11, (String) row.get("t9"));

                    stmt.addBatch();
                } catch (Exception e) {
                    System.err.println("Мөр хадгалахад алдаа: " + row + " -> " + e.getMessage());
                }
            }

            stmt.executeBatch();
            System.out.println("Шинэ " + newRows.size() + " мөр амжилттай нэмэгдлээ.");

        } catch (SQLException e) {
            System.err.println("Шинэ мөрүүдийг хадгалахад алдаа: " + e.getMessage());
        }
    }

    private Date parseSqlDate(Object value) {
        if (value == null) return null;
        try {
            LocalDate date = LocalDate.parse(value.toString().substring(0, 10));
            return Date.valueOf(date);
        } catch (Exception e) {
            System.err.println("Огноо хөрвүүлэхэд алдаа: " + value + " -> " + e.getMessage());
            return null;
        }
    }

    private Timestamp parseSqlTimestamp(Object value) {
        if (value == null) return null;
        try {
            return Timestamp.valueOf(value.toString());
        } catch (Exception e) {
            System.err.println("Огноо цаг хөрвүүлэхэд алдаа: " + value + " -> " + e.getMessage());
            return null;
        }
    }

	@Override
	public void syncAllFromRemoteTname() {
	
		int page = 0;
        int size = 1000;

        while (true) {
            String url = remoteApiUrl + "/tnamelist?page=" + page + "&size=" + size;

            List<Map<String, Object>> dataList;
            try {
                dataList = restTemplate.getForObject(url, List.class);
            } catch (Exception e) {
                System.err.println("Алдаа: " + e.getMessage());
                break;
            }

            if (dataList.isEmpty()) {
                System.out.println("Датаны төгсгөлд хүрлээ. Нийт амжилттай татсан хуудсууд: " + page);
                break;
            }
            
            saveOrUpdateToLocalDatabaseTname(dataList);
            System.out.println("Page " + page + ": " + dataList.size() + " мөр хадгалагдлаа");

            page++;
		
	}
	
	}
	
	private void saveOrUpdateToLocalDatabaseTname(List<Map<String, Object>> dataList) {

	    String sql =
	        "MERGE INTO a_name AS target " +
	        "USING (SELECT ? AS kid, ? AS tid, ? AS n) AS source " +
	        "ON target.kid = source.kid " +
	        "WHEN MATCHED AND ( " +
	        "    ISNULL(target.tid, -1) <> ISNULL(source.tid, -1) OR " +
	        "    ISNULL(target.n, '') <> ISNULL(source.n, '') " +
	        ") THEN UPDATE SET " +
	        "    tid = source.tid, " +
	        "    n = source.n " +
	        "WHEN NOT MATCHED THEN " +
	        "INSERT (kid, tid, n) " +
	        "VALUES (source.kid, source.tid, source.n);";

	    try (Connection conn = dataSource.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        for (Map<String, Object> row : dataList) {
	            try {
	                stmt.setInt(1, (Integer) row.get("kid"));
	                stmt.setInt(2, (Integer) row.get("tid"));
	                stmt.setString(3, (String) row.get("n"));
	                stmt.addBatch();
	            } catch (Exception e) {
	                System.err.println("Мөр боловсруулахад алдаа: " + row + " -> " + e.getMessage());
	            }
	        }

	        stmt.executeBatch();

	    } catch (SQLException e) {
	        System.err.println("Batch MERGE хийхэд алдаа: " + e.getMessage());
	    }
	}

	@Override
	public int syncAllFromRemoteDelo() {
	    int page = 0;
	    int size = 1000;
	    int totalSaved = 0;

	    while (true) {
	        String url = remoteApiUrl + "/delolist?page=" + page + "&size=" + size;

	        List<Map<String, Object>> dataList;
	        try {
	            dataList = restTemplate.getForObject(url, List.class);
	        } catch (Exception e) {
	            System.err.println("Алдаа: " + e.getMessage());
	            break;
	        }

	        if (dataList.isEmpty()) {
	            break;
	        }

	        int saved = saveOrUpdateToLocalDatabaseDelo(dataList);
	        totalSaved += saved;

	        System.out.println("Page " + page + ": " + saved + " мөр хадгалагдлаа");
	        page++;
	    }

	    return totalSaved; // эцсийн хадгалсан тоог буцаана
	}


	
	private int saveOrUpdateToLocalDatabaseDelo(List<Map<String, Object>> dataList) {
	    String sql =
	        "MERGE INTO a_delo AS target " +
	        "USING (SELECT ? AS uid, ? AS un, ? AS oid, ? AS u1, ? AS u2, ? AS u3, ? AS u4, " +
	        "? AS u5, ? AS u6, ? AS u7, ? AS u8, ? AS u9, ? AS u10, ? AS u11) AS source " +
	        "ON target.uid = source.uid " +
	        "WHEN MATCHED AND ( " +
	        "    ISNULL(target.un, -1) <> ISNULL(source.un, -1) OR " +
	        "    ISNULL(target.oid, '') <> ISNULL(source.oid, '') OR " +
	        "    ISNULL(target.u1, '') <> ISNULL(source.u1, '') OR " +
	        "    ISNULL(target.u2, -1) <> ISNULL(source.u2, -1) OR " +
	        "    ISNULL(target.u3, '') <> ISNULL(source.u3, '') OR " +
	        "    ISNULL(target.u4, -1) <> ISNULL(source.u4, -1) OR " +
	        "    ISNULL(target.u5, '1900-01-01') <> ISNULL(source.u5, '1900-01-01') OR " +
	        "    ISNULL(target.u6, '1900-01-01') <> ISNULL(source.u6, '1900-01-01') OR " +
	        "    ISNULL(target.u7, -1) <> ISNULL(source.u7, -1) OR " +
	        "    ISNULL(target.u8, '') <> ISNULL(source.u8, '') OR " +
	        "    ISNULL(target.u9, '') <> ISNULL(source.u9, '') OR " +
	        "    ISNULL(target.u10, -1) <> ISNULL(source.u10, -1) OR " +
	        "    ISNULL(target.u11, -1) <> ISNULL(source.u11, -1) " +
	        ") THEN UPDATE SET " +
	        "    un = source.un, oid = source.oid, u1 = source.u1, u2 = source.u2, " +
	        "    u3 = source.u3, u4 = source.u4, u5 = source.u5, u6 = source.u6, " +
	        "    u7 = source.u7, u8 = source.u8, u9 = source.u9, u10 = source.u10, u11 = source.u11 " +
	        "WHEN NOT MATCHED THEN " +
	        "INSERT (uid, un, oid, u1, u2, u3, u4, u5, u6, u7, u8, u9, u10, u11) " +
	        "VALUES (source.uid, source.un, source.oid, source.u1, source.u2, source.u3, source.u4, " +
	        "source.u5, source.u6, source.u7, source.u8, source.u9, source.u10, source.u11);";

	    int count = 0;

	    try (Connection conn = dataSource.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        for (Map<String, Object> row : dataList) {
	            try {
	                stmt.setInt(1, (Integer) row.get("uid"));
	                stmt.setInt(2, (Integer) row.get("un"));
	                stmt.setString(3, row.get("oid") != null ? row.get("oid").toString() : null);
	                stmt.setString(4, (String) row.get("u1"));
	                stmt.setInt(5, (Integer) row.get("u2"));
	                stmt.setString(6, row.get("u3") != null ? row.get("u3").toString() : null);
	                stmt.setInt(7, (Integer) row.get("u4"));
	                stmt.setDate(8, parseSqlDate(row.get("u5")));
	                stmt.setDate(9, parseSqlDate(row.get("u6")));
	                stmt.setObject(10, row.get("u7"), Types.INTEGER);
	                stmt.setString(11, row.get("u8") != null ? row.get("u8").toString() : null);
	                stmt.setString(12, row.get("u9") != null ? row.get("u9").toString() : null);
	                stmt.setObject(13, row.get("u10"), Types.INTEGER);
	                stmt.setObject(14, row.get("u11"), Types.INTEGER);

	                stmt.addBatch();
	                count++;
	            } catch (Exception e) {
	                System.err.println("Мөр боловсруулахад алдаа: " + row + " -> " + e.getMessage());
	            }
	        }

	        stmt.executeBatch();

	    } catch (SQLException e) {
	        System.err.println("Batch MERGE хийхэд алдаа: " + e.getMessage());
	    }

	    return count;
	}
	
	
	public class SyncResult {
	    private int inserted;
	    private int updated;

	    public SyncResult(int inserted, int updated) {
	        this.inserted = inserted;
	        this.updated = updated;
	    }

	    public int getInserted() { return inserted; }
	    public int getUpdated() { return updated; }
	}

}
