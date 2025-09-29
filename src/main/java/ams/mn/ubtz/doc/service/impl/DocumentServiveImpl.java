package ams.mn.ubtz.doc.service.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ams.mn.ubtz.config.DataSourceAmsoProperties;
import ams.mn.ubtz.config.DataSourceProperties;
import ams.mn.ubtz.doc.entity.Document;
import ams.mn.ubtz.doc.entity.DocumentLog;
import ams.mn.ubtz.doc.service.DocumentService;


@Service("DocService")
public class DocumentServiveImpl implements DocumentService {
	
	private final DataSourceProperties dataSourceProperties;
	private final DataSourceAmsoProperties dataSourceAmsoProperties;
	 
	public DocumentServiveImpl(DataSourceProperties dataSourceProperties, DataSourceAmsoProperties dataSourceAmsoProperties) {
	   this.dataSourceProperties = dataSourceProperties;
	   this.dataSourceAmsoProperties = dataSourceAmsoProperties;
	}
		
	@Override
	public List<Object> docList(String pid) {
		
		List<Object> list = new ArrayList<>();
        String sql = "SELECT npid,pid,t1,t2,t3,t4,t5,t6,u.wname,t8,t9,t7 FROM tushaal, users u WHERE tushaal.t7=u.uid AND pid='"+ pid +"' AND tushaal.is_delete=0";
      
        try (
            Connection conn = DriverManager.getConnection(dataSourceProperties.getUrl());
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("npid", rs.getInt("npid"));
                row.put("pid", rs.getString("pid"));
                row.put("t1", rs.getString("t1"));
                row.put("t2", rs.getDate("t2"));
                row.put("t3", rs.getString("t3"));
                row.put("t4", rs.getString("t4"));
                row.put("t5", rs.getString("t5"));
                row.put("t6", rs.getString("t6"));
                row.put("wname", rs.getString("wname"));
                row.put("t8", rs.getDate("t8"));
                row.put("t9", rs.getString("t9"));
                row.put("t7", rs.getInt("t7"));
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
	}

	@Override
	public List<Object> docSelect(int npid) {
		
		List<Object> list = new ArrayList<>();
        String sql = "SELECT pid,t1,t2,t3,t4,t5,t6,u.wname,t8,t9 FROM tushaal, users u WHERE tushaal.t7=u.uid AND npid="+ npid +"";
      
        try (
            Connection conn = DriverManager.getConnection(dataSourceProperties.getUrl());
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("pid", rs.getString("pid"));
                row.put("t1", rs.getString("t1"));
                row.put("t2", rs.getDate("t2"));
                row.put("t3", rs.getString("t3"));
                row.put("t4", rs.getString("t4"));
                row.put("t5", rs.getString("t5"));
                row.put("t6", rs.getString("t6"));
                row.put("wname", rs.getString("wname"));
                row.put("t8", rs.getDate("t8"));
                row.put("t9", rs.getString("t9"));
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
	}

	@Override
	public void docInsert(Document obj) {
	    Connection conn = null;
	    PreparedStatement myStmt = null;

	    String query = "INSERT INTO tushaal (pid,t1,t2,t3,t4,t5,t6,t7,t8,t9) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	    try {
	        conn = DriverManager.getConnection(dataSourceProperties.getUrl());
	        myStmt = conn.prepareStatement(query);
	        
	        myStmt.setString(1, obj.getPid());
	        myStmt.setString(2, obj.getT1());
	        
	        // LocalDateTime -> java.sql.Date хөрвүүлэлт
	        if (obj.getT2() != null) {
	            myStmt.setDate(3, java.sql.Date.valueOf(obj.getT2()));
	        } else {
	            myStmt.setNull(3, java.sql.Types.DATE);
	        }
	        
	        myStmt.setString(4, obj.getT3());
	        myStmt.setString(5, obj.getT4());
	        myStmt.setString(6, obj.getT5());
	        myStmt.setString(7, obj.getT6());
	        myStmt.setInt(8, obj.getT7());
	        
	        if (obj.getT8() != null) {
	            myStmt.setTimestamp(9, java.sql.Timestamp.valueOf(obj.getT8()));
	        } else {
	            myStmt.setNull(9, java.sql.Types.TIMESTAMP);
	        }
	        
	        myStmt.setString(10, obj.getT9());
	      
	        myStmt.executeUpdate();

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            if (myStmt != null) myStmt.close();
	            if (conn != null) conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	    }
		
	}

	@Override
	public void docUpdate(Document obj) {
		
		Connection conn = null;
	    PreparedStatement myStmt = null;

	    String query = "UPDATE tushaal SET pid=?, t1=?,t2=?,t3=?,t4=?,t5=?,t6=?,t9=? WHERE npid=?";

	    try {
	        conn = DriverManager.getConnection(dataSourceProperties.getUrl());
	        myStmt = conn.prepareStatement(query);
	        
	        myStmt.setString(1, obj.getPid());
	        myStmt.setString(2, obj.getT1());
	        
	        // LocalDateTime -> java.sql.Date хөрвүүлэлт
	        if (obj.getT2() != null) {
	            myStmt.setDate(3, java.sql.Date.valueOf(obj.getT2()));
	        } else {
	            myStmt.setNull(3, java.sql.Types.DATE);
	        }
	        
	        myStmt.setString(4, obj.getT3());
	        myStmt.setString(5, obj.getT4());
	        myStmt.setString(6, obj.getT5());
	        myStmt.setString(7, obj.getT6());
	        /*myStmt.setInt(8, obj.getT7());
	        
	        if (obj.getT8() != null) {
	            myStmt.setTimestamp(9, java.sql.Timestamp.valueOf(obj.getT8()));
	        } else {
	            myStmt.setNull(9, java.sql.Types.TIMESTAMP);
	        }*/
	        
	        myStmt.setString(8, obj.getT9());
	        myStmt.setInt(9, obj.getNpid());
	      
	        myStmt.executeUpdate();

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            if (myStmt != null) myStmt.close();
	            if (conn != null) conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	    }
		
	}

	@Override
	public void docLogInsert(DocumentLog obj) {
		
		Connection conn = null;
	    PreparedStatement myStmt = null;

	    String query = "INSERT INTO log_tushaal (npid,type,uid,log_date) VALUES (?, ?, ?, ?)";

	    try {
	        conn = DriverManager.getConnection(dataSourceProperties.getUrl());
	        myStmt = conn.prepareStatement(query);
	        
	        myStmt.setInt(1, obj.getNpid());
	        myStmt.setString(2, obj.getType());
	        myStmt.setInt(3, obj.getUid());
	        myStmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

	        /*if (obj.getLogDate() != null) {
	            myStmt.setTimestamp(4, java.sql.Timestamp.valueOf(obj.getLogDate()));
	        } else {
	            myStmt.setNull(4, java.sql.Types.TIMESTAMP);
	        }*/
	        
	        myStmt.executeUpdate();

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            if (myStmt != null) myStmt.close();
	            if (conn != null) conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	    }
		
	}

	@Override
	public void docDelete(int npid, int uid) {
		
	    Connection conn = null;
	    PreparedStatement stmt1 = null;
	    PreparedStatement stmt2 = null;

	    String query1 = "UPDATE tushaal SET is_delete=1 WHERE npid=?";
	    String query2 = "INSERT INTO log_tushaal (npid, type, uid, log_date) VALUES (?, ?, ?, ?)";

	    try {
	        conn = DriverManager.getConnection(dataSourceProperties.getUrl());

	        // 1. Устгах тэмдэглэгээ хийх
	        stmt1 = conn.prepareStatement(query1);        
	        stmt1.setInt(1, npid);
	        stmt1.executeUpdate();

	        // 2. Лог бүртгэх
	        stmt2 = conn.prepareStatement(query2);        
	        stmt2.setInt(1, npid);
	        stmt2.setString(2, "deleted");
	        stmt2.setInt(3, uid);
	        stmt2.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
	        stmt2.executeUpdate();

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            if (stmt1 != null) stmt1.close();
	            if (stmt2 != null) stmt2.close();
	            if (conn != null) conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	    }
	}

	@Override
	public List<Object> tname(int tid) {
		
		List<Object> list = new ArrayList<>();
        String sql = "SELECT kid,tid,n FROM t_name WHERE tid="+ tid +" ORDER BY kid";
      
        try (
            Connection conn = DriverManager.getConnection(dataSourceProperties.getUrl());
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("kid", rs.getInt("kid"));
                row.put("tid", rs.getInt("tid"));
                row.put("n", rs.getString("n"));
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
	}

	@Override
	public List<Object> docListDown() {
		
		List<Object> list = new ArrayList<>();
        String sql = "select t.npid, t.pid,t.t1,t.t2,t.t3,t.t4,t.t5,t.t6,t.t7,t.t8,t.t9,u.org from tushaal t left join users u on t.t7=u.uid where u.org>=7739 and t.t2<'2017-01-01'";
      
        try (
            Connection conn = DriverManager.getConnection(dataSourceAmsoProperties.getUrl());
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("npid", rs.getInt("npid"));
                row.put("pid", rs.getString("pid"));
                row.put("t1", rs.getString("t1"));
                row.put("t2", rs.getDate("t2"));
                row.put("t3", rs.getString("t3"));
                row.put("t4", rs.getString("t4"));
                row.put("t5", rs.getString("t5"));
                row.put("t6", rs.getString("t6"));
                row.put("t7", rs.getInt("t7"));
                row.put("t8", rs.getDate("t8"));
                row.put("t9", rs.getString("t9"));
                row.put("org", rs.getInt("org"));
                
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
	}

	@Override
	public List<Map<String, Object>> getPagedData(int page, int size) {
	    List<Map<String, Object>> list = new ArrayList<>();

	    try (
	        Connection conn = DriverManager.getConnection(dataSourceAmsoProperties.getUrl());
	        CallableStatement stmt = conn.prepareCall("{call getPagedDataDoc(?, ?)}")
	    ) {
	        stmt.setInt(1, page); // зөвхөн page дамжуулна
	        stmt.setInt(2, size);

	        ResultSet rs = stmt.executeQuery();
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	        while (rs.next()) {
	            Map<String, Object> row = new LinkedHashMap<>();
	            row.put("npid", rs.getInt("npid"));
	            row.put("pid", rs.getString("pid"));
	            row.put("t1", rs.getString("t1"));
	            row.put("t2", rs.getDate("t2"));
	            row.put("t3", rs.getString("t3"));
	            row.put("t4", rs.getString("t4"));
	            row.put("t5", rs.getString("t5"));
	            row.put("t6", rs.getString("t6"));
	            row.put("t7", rs.getInt("t7"));
	            row.put("t8", sdf.format(rs.getTimestamp("t8")));
	            row.put("t9", rs.getString("t9"));
	            row.put("org", rs.getInt("org"));
	            list.add(row);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return list;
	}

	@Override
	public List<Map<String, Object>> getTnameData(int page, int size) {
	    List<Map<String, Object>> list = new ArrayList<>();

	    String sql = "SELECT n.kid, n.tid, n.n FROM tushaal t " +
	                 "LEFT JOIN users u ON t.t7 = u.uid " +
	                 "INNER JOIN t_name n ON n.tid = t.npid " +
	                 "WHERE (u.org >= 7739 OR u.org = 7663) AND t.t2 < '2015-01-01' " +
	                 "ORDER BY n.kid " +
	                 "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

	    int offset = page * size;

	    try (
	        Connection conn = DriverManager.getConnection(dataSourceAmsoProperties.getUrl());
	        PreparedStatement stmt = conn.prepareStatement(sql)
	    ) {
	        stmt.setInt(1, offset);
	        stmt.setInt(2, size);

	        ResultSet rs = stmt.executeQuery();
	        
	        while (rs.next()) {
	            Map<String, Object> row = new LinkedHashMap<>();
	            row.put("kid", rs.getInt("kid"));
	            row.put("tid", rs.getInt("tid"));
	            row.put("n", rs.getString("n"));

	            list.add(row);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return list;
	}

	@Override
	public List<Map<String, Object>> getUnitData(int page, int size) {
	    List<Map<String, Object>> list = new ArrayList<>();

	    String sql = "SELECT u.uid, u.un, u.oid, u.u1, u.u2, u.u3, u.u4, u.u5, u.u6, " +
	                 "u.u7, u.u8, u.u9, u.u10, u.u11 " +
	                 "FROM unit u " +
	                 "LEFT JOIN opus o ON u.oid = o.oid " +
	                 "LEFT JOIN fund f ON o.fid = f.fid " +
	                 "WHERE f.f8 > 7738 OR f.f8 = 7663" +
	                 "ORDER BY u.uid " +
	                 "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

	    int offset = page * size;

	    try (
	        Connection conn = DriverManager.getConnection(dataSourceAmsoProperties.getUrl());
	        PreparedStatement stmt = conn.prepareStatement(sql)
	    ) {
	        stmt.setInt(1, offset);
	        stmt.setInt(2, size);

	        ResultSet rs = stmt.executeQuery();

	        while (rs.next()) {
	            Map<String, Object> row = new LinkedHashMap<>();
	            row.put("uid", rs.getInt("uid"));
	            row.put("un", rs.getInt("un"));
	            row.put("oid", rs.getInt("oid"));
	            row.put("u1", rs.getString("u1"));
	            row.put("u2", rs.getInt("u2"));
	            row.put("u3", rs.getString("u3"));
	            row.put("u4", rs.getInt("u4"));
	            row.put("u5", rs.getDate("u5"));
	            row.put("u6", rs.getDate("u6"));
	            row.put("u7", rs.getObject("u7"));     // Nullable Integer
	            row.put("u8", rs.getString("u8"));
	            row.put("u9", rs.getString("u9"));
	            row.put("u10", rs.getObject("u10"));   // Nullable Integer
	            row.put("u11", rs.getObject("u11"));   // Nullable Integer

	            list.add(row);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return list;
	}
	
	@Override
	public List<Object> docListReport(Date fromDate, Date toDate, Integer uid) {
	    List<Object> list = new ArrayList<>();

	    StringBuilder sql = new StringBuilder();
	    sql.append("SELECT f.fid, f.fkod, o.okod, o.oid, t.pid, d.l1, t.npid, ");
	    sql.append("t.t1, t.t2, t.t3, t.t4, t.t5, t.t6, u.wname, u.uid, t.t8, t.t9, t.t7 ");
	    sql.append("FROM tushaal t ");
	    sql.append("JOIN users u ON t.t7 = u.uid ");
	    sql.append("JOIN delo d ON d.lid = t.pid ");
	    sql.append("JOIN opis o ON o.oid = d.opis ");
	    sql.append("JOIN fond f ON f.fid = o.fond ");
	    sql.append("WHERE t.is_delete = 0 ");

	    List<Object> params = new ArrayList<>();

	    if (fromDate != null && toDate != null) {
	        sql.append("AND t.t8 BETWEEN ? AND ? ");
	        params.add(new java.sql.Date(fromDate.getTime()));
	        params.add(new java.sql.Date(toDate.getTime()));
	    } else if (fromDate != null) {
	        sql.append("AND t.t8 >= ? ");
	        params.add(new java.sql.Date(fromDate.getTime()));
	    } else if (toDate != null) {
	        sql.append("AND t.t8 <= ? ");
	        params.add(new java.sql.Date(toDate.getTime()));
	    }

	    if (uid != null) {
	        sql.append("AND u.uid = ? ");
	        params.add(uid);
	    }

	    try (
	        Connection conn = DriverManager.getConnection(dataSourceProperties.getUrl());
	        PreparedStatement stmt = conn.prepareStatement(sql.toString())
	    ) {
	        for (int i = 0; i < params.size(); i++) {
	            stmt.setObject(i + 1, params.get(i));
	        }

	        try (ResultSet rs = stmt.executeQuery()) {
	            while (rs.next()) {
	                Map<String, Object> row = new LinkedHashMap<>();
	                row.put("fid", rs.getString("fid"));
	                row.put("fkod", rs.getString("fkod"));
	                row.put("okod", rs.getString("okod"));
	                row.put("oid", rs.getString("oid"));
	                row.put("pid", rs.getString("pid"));
	                row.put("l1", rs.getString("l1"));
	                row.put("npid", rs.getInt("npid"));
	                row.put("t1", rs.getString("t1"));
	                row.put("t2", rs.getDate("t2"));
	                row.put("t3", rs.getString("t3"));
	                row.put("t4", rs.getString("t4"));
	                row.put("t5", rs.getString("t5"));
	                row.put("t6", rs.getString("t6"));
	                row.put("wname", rs.getString("wname"));
	                row.put("uid", rs.getInt("uid"));
	                row.put("t8", rs.getTimestamp("t8"));
	                row.put("t9", rs.getString("t9"));
	                row.put("t7", rs.getInt("t7"));
	                list.add(row);
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return list;
	}

	
	/*public List<Map<String, Object>> getPagedData(int page, int size) {
	    List<Map<String, Object>> list = new ArrayList<>();

	    /*String sql = "SELECT t.npid, t.pid, t.t1, t.t2, t.t3, t.t4, t.t5, t.t6, t.t7, t.t8, t.t9, u.org " +
	                 "FROM tushaal t " +
	                 "LEFT JOIN users u ON t.t7 = u.uid " +
	                 "WHERE u.org >= 7739 AND t.t2 < '2017-01-01' " +
	                 "ORDER BY t.npid " +
	                 "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
	    
	    String sql = "EXEC [dbo].[getPagedDataDoc] @Page=?, @Size=?";

	    //int offset = page * size;

	    try (
	        Connection conn = DriverManager.getConnection(dataSourceAmsoProperties.getUrl());
	        PreparedStatement stmt = conn.prepareStatement(sql)
	    ) {
	        stmt.setInt(1, page);
	        stmt.setInt(2, size);

	        ResultSet rs = stmt.executeQuery();
	        
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        
	        while (rs.next()) {
	            Map<String, Object> row = new LinkedHashMap<>();
	            row.put("npid", rs.getInt("npid"));
	            row.put("pid", rs.getString("pid"));
	            row.put("t1", rs.getString("t1"));
	            row.put("t2", rs.getDate("t2"));
	            row.put("t3", rs.getString("t3"));
	            row.put("t4", rs.getString("t4"));
	            row.put("t5", rs.getString("t5"));
	            row.put("t6", rs.getString("t6"));
	            row.put("t7", rs.getInt("t7"));
	            row.put("t8", sdf.format(rs.getTimestamp("t8")));
	            row.put("t9", rs.getString("t9"));
	            row.put("org", rs.getInt("org"));

	            list.add(row);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return list;
	}*/

	/*public List<Map<String, Object>> getPagedData(int page, int size) {
        
		List<Map<String, Object>> list = new ArrayList<>();

        String sql = "SELECT npid,t1,t2,t3,t4,t5,t6,t7,t8,t9,org FROM (" +
                     "  SELECT t.npid,t.t1,t.t2,t.t3,t.t4,t.t5,t.t6,t.t7,t.t8,t.t9,u.org, " +
                     "         ROW_NUMBER() OVER (ORDER BY t.npid) AS rn " +
                     "  FROM tushaal t " +
                     "  LEFT JOIN users u ON t.t7=u.uid " +
                     "  WHERE u.org >= 7739 AND t.t2 < '2017-01-01'" +
                     ") AS sub " +
                     "WHERE sub.rn BETWEEN ? AND ?";

        int from = page * size + 1;
        int to = from + size - 1;

        try (
            Connection conn = DriverManager.getConnection(dataSourceAmsoProperties.getUrl());
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, from);
            stmt.setInt(2, to);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("npid", rs.getInt("npid"));
                row.put("t1", rs.getString("t1"));
                row.put("t2", rs.getDate("t2"));
                row.put("t3", rs.getString("t3"));
                row.put("t4", rs.getString("t4"));
                row.put("t5", rs.getString("t5"));
                row.put("t6", rs.getString("t6"));
                row.put("t7", rs.getInt("t7"));
                row.put("t8", rs.getDate("t8"));
                row.put("t9", rs.getString("t9"));
                row.put("org", rs.getInt("org"));

                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
	}*/

	

}
