package ams.mn.ubtz.delo.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ams.mn.ubtz.config.DataSourceProperties;
import ams.mn.ubtz.delo.entity.Delo;
import ams.mn.ubtz.delo.service.DeloService;


@Service("DeloService")
public class DeloServiceImpl implements DeloService{
	
    private final DataSourceProperties dataSourceProperties;
	
	public DeloServiceImpl(DataSourceProperties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

	@Override
	public List<Object> deloList(String oid) {
		
		List<Object> list = new ArrayList<>();
        //String sql = "SELECT oid, fond, okod, oname, g3,g4,g24,g14 FROM opis where fond='"+ oid +"' order by convert(int,okod)";
        String sql = "SELECT lid,l1,opis,l4,l8,l9,l11,l26,l14 FROM delo where opis='"+ oid +"'";
      
        try (
            Connection conn = DriverManager.getConnection(dataSourceProperties.getUrl());
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("lid", rs.getString("lid"));
                row.put("l1", rs.getString("l1"));
                row.put("oid", rs.getString("opis"));
                row.put("l4", rs.getString("l4"));
                row.put("l8", rs.getDate("l8"));
                row.put("l9", rs.getDate("l9"));
                /*row.put("l10", rs.getString("l10"));
                row.put("l11", rs.getInt("l11"));
                row.put("l12", rs.getInt("l12"));
                row.put("l13", rs.getInt("l13"));*/
                row.put("l14", rs.getString("l14"));
               /* row.put("l15", rs.getBoolean("l15"));
                row.put("l16", rs.getBoolean("l16"));
                row.put("l18", rs.getInt("l18"));
                row.put("l19", rs.getBoolean("l19"));
                row.put("l20", rs.getBoolean("l20"));
                row.put("l21", rs.getBoolean("l21"));
                row.put("l22", rs.getBoolean("l22"));
                row.put("l23", rs.getBoolean("l23"));
                row.put("l24", rs.getString("l24"));*/
                row.put("l26", rs.getString("l26"));
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
	}
	
	@Override
	public List<Object> getFidOid(String fkod, String okod) {
		
		List<Object> list = new ArrayList<>();
        String sql ="SELECT f.fid,f.fkod,f.fname, o.oid, o.okod,o.oname FROM opis o, fond f WHERE f.fid=o.fond and f.fkod='"+ fkod +"' and o.okod='"+ okod +"'";
		//String sql ="SELECT f.fid,f.fkod, d.opis,o.okod, d.lid,d.l1,d.opis,d.l4,d.l8,d.l9,d.l11,d.l26,d.l14 FROM delo d, opis o, fond f where d.opis=o.oid and f.fid=o.fond and f.fkod='"+ fkod +"' and o.okod='"+ okod+"'";

        try (
            Connection conn = DriverManager.getConnection(dataSourceProperties.getUrl());
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
            	 Map<String, Object> row = new LinkedHashMap<>();
            	 row.put("fid", rs.getString("fid"));
            	 row.put("fkod", rs.getString("fkod"));
                 row.put("oid", rs.getString("oid"));
                 row.put("okod", rs.getString("okod"));
                 list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
	}

	@Override
	public List<Object> deloSelect(String lid) {
		
		List<Object> list = new ArrayList<>();
        String sql = "SELECT lid, l1, opis, l4, l5, l8, l9, l10, l11, l12, l13, l14, l18, l24, l26 FROM delo where lid='"+ lid +"'";
      
        try (
            Connection conn = DriverManager.getConnection(dataSourceProperties.getUrl());
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("lid", rs.getString("lid"));
                row.put("l1", rs.getString("l1"));
                row.put("oid", rs.getString("opis"));
                row.put("l4", rs.getString("l4"));
                row.put("l5", rs.getString("l5"));
                row.put("l8", rs.getDate("l8"));
                row.put("l9", rs.getDate("l9"));
                row.put("l10", rs.getString("l10"));
                row.put("l11", rs.getInt("l11"));
                row.put("l12", rs.getInt("l12"));
                row.put("l13", rs.getInt("l13"));
                row.put("l14", rs.getString("l14"));
               /* row.put("l15", rs.getBoolean("l15"));
                row.put("l16", rs.getBoolean("l16"));*/
                row.put("l18", rs.getInt("l18"));
                /*row.put("l19", rs.getBoolean("l19"));
                row.put("l20", rs.getBoolean("l20"));
                row.put("l21", rs.getBoolean("l21"));
                row.put("l22", rs.getBoolean("l22"));
                row.put("l23", rs.getBoolean("l23"));*/
                row.put("l24", rs.getString("l24"));
                row.put("l26", rs.getString("l26"));
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
	}

	@Override
	public void deloInsert(Delo obj) {

	    Connection conn = null;
	    PreparedStatement myStmt = null;

	    String query = "INSERT INTO delo (lid, l1, opis, l4, l5, l8, l9, l10, l11, l12, l13, l14, l18, l24, l26) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	    try {
	        conn = DriverManager.getConnection(dataSourceProperties.getUrl());
	        myStmt = conn.prepareStatement(query);

	        myStmt.setString(1, obj.getLid());
	        myStmt.setString(2, obj.getL1());
	        myStmt.setString(3, obj.getOpis());
	        myStmt.setString(4, obj.getL4());
	        myStmt.setString(5, obj.getL5());

	        // LocalDateTime -> java.sql.Date хөрвүүлэлт
	        if (obj.getL8() != null) {
	            myStmt.setDate(6, java.sql.Date.valueOf(obj.getL8()));
	        } else {
	            myStmt.setNull(6, java.sql.Types.DATE);
	        }

	        if (obj.getL9() != null) {
	            myStmt.setDate(7, java.sql.Date.valueOf(obj.getL9()));
	        } else {
	            myStmt.setNull(7, java.sql.Types.DATE);
	        }

	        myStmt.setString(8, obj.getL10());
	        myStmt.setInt(9, obj.getL11());

	        myStmt.setInt(10, obj.getL12());
	        myStmt.setInt(11, obj.getL13());
	       
	        myStmt.setString(12, obj.getL14());
	        myStmt.setInt(13, obj.getL18());
	        myStmt.setString(14, obj.getL24());
	        myStmt.setString(15, obj.getL26());

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
	public void deloUpdate(Delo obj) {
		
		Connection conn = null;
	    PreparedStatement myStmt = null;

	    String query = "UPDATE delo SET l1= ?, opis= ?, l4= ?, l5= ?, l8= ?, l9= ?, l10= ?, l11= ?, l12= ?, l13= ?, l14= ?, l18= ?, l24= ?, l26= ? WHERE lid = ?";
	    
	    try {
	        conn = DriverManager.getConnection(dataSourceProperties.getUrl());
	        myStmt = conn.prepareStatement(query);

	        myStmt.setString(1, obj.getL1());
	        myStmt.setString(2, obj.getOpis());
	        myStmt.setString(3, obj.getL4());
	        myStmt.setString(4, obj.getL5());
	        
	        System.out.println(java.sql.Date.valueOf(obj.getL8()));

	        // LocalDateTime -> java.sql.Date хөрвүүлэлт
	        if (obj.getL8() != null) {
	            myStmt.setDate(5, java.sql.Date.valueOf(obj.getL8()));
	        } else {
	            myStmt.setNull(5, java.sql.Types.DATE);
	        }

	        if (obj.getL9() != null) {
	            myStmt.setDate(6, java.sql.Date.valueOf(obj.getL9()));
	        } else {
	            myStmt.setNull(6, java.sql.Types.DATE);
	        }

	        myStmt.setString(7, obj.getL10());
	        myStmt.setInt(8, obj.getL11());

	        myStmt.setInt(9, obj.getL12());
	        myStmt.setInt(10, obj.getL13());
	       
	        myStmt.setString(11, obj.getL14());
	        myStmt.setInt(12, obj.getL18());
	        myStmt.setString(13, obj.getL24());
	        myStmt.setString(14, obj.getL26());
	        
	        myStmt.setString(15, obj.getLid());

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

	

}
