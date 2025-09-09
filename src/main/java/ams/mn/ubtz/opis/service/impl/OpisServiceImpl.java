package ams.mn.ubtz.opis.service.impl;

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
import ams.mn.ubtz.opis.entity.Opis;
import ams.mn.ubtz.opis.service.OpisService;

@Service("OpisService")
public class OpisServiceImpl implements OpisService {
	
	private final DataSourceProperties dataSourceProperties;
	
	public OpisServiceImpl(DataSourceProperties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

	@Override
	public List<Object> opisList(String fid) {
		
		List<Object> list = new ArrayList<>();
        String sql = "SELECT oid, fond, okod, oname, g3,g4,g24,g14 FROM opis where fond='"+ fid +"' order by convert(int,okod)";

        try (
            Connection conn = DriverManager.getConnection(dataSourceProperties.getUrl());
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("oid", rs.getString("oid"));
                row.put("okod", rs.getString("okod"));
                row.put("fid", rs.getString("fond"));
                row.put("oname", rs.getString("oname"));
                //row.put("g1", rs.getInt("g1"));
                row.put("g3", rs.getString("g3"));
                row.put("g4", rs.getString("g4"));
               // row.put("g6", rs.getInt("g6"));
               // row.put("g13", rs.getInt("g13"));
                row.put("g14", rs.getString("g14"));
               // row.put("g16", rs.getInt("g16"));
               // row.put("g17", rs.getInt("g17"));
                row.put("g24", rs.getInt("g24"));
                //row.put("g28", rs.getInt("g28"));
                //row.put("g29", rs.getInt("g29"));
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
	}

	@Override
	public List<Object> getSelect(String oid) {
		
		List<Object> list = new ArrayList<>();
        String sql = "SELECT oid, fond, okod, oname, g3,g4,g24,g13,g14 FROM opis where oid ='"+ oid +"'";

        try (
            Connection conn = DriverManager.getConnection(dataSourceProperties.getUrl());
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("oid", rs.getString("oid"));
                row.put("okod", rs.getString("okod"));
                row.put("fid", rs.getString("fond"));
                row.put("oname", rs.getString("oname"));
                //row.put("g1", rs.getInt("g1"));
                row.put("g3", rs.getString("g3"));
                row.put("g4", rs.getString("g4"));
               // row.put("g6", rs.getInt("g6"));
                row.put("g13", rs.getInt("g13"));
                row.put("g14", rs.getString("g14"));
               // row.put("g16", rs.getInt("g16"));
               // row.put("g17", rs.getInt("g17"));
                row.put("g24", rs.getInt("g24"));
                //row.put("g28", rs.getInt("g28"));
                //row.put("g29", rs.getInt("g29"));
                list.add(row);
                
               
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
	}

	@Override
	public void opisInsert(Opis obj) {
		
		Connection conn = null; 
        PreparedStatement myStmt = null;
        String query = "INSERT INTO opis (oid,okod,fond,oname,g3,g4,g13,g14,g24) VALUES (?,?,?,?,?,?,?,?,?)";

       try{
    	   conn = DriverManager.getConnection(dataSourceProperties.getUrl());
           myStmt = conn.prepareStatement(query);
           
           myStmt.setString(1,obj.getOid());
           myStmt.setString(2,obj.getOkod());
           myStmt.setString(3,obj.getFond());
           myStmt.setString(4,obj.getOname());
           myStmt.setInt(5,obj.getG3());
           myStmt.setInt(6,obj.getG4());
           myStmt.setInt(7,obj.getG13());
           myStmt.setString(8,obj.getG14());
           myStmt.setInt(9,obj.getG24());
          
           myStmt.executeUpdate();
           conn.close();
           
           myStmt.close();
        
       }catch (Exception e){
    	   e.printStackTrace();
       }
		
	}

	@Override
	public void opisUpdate(Opis obj) {
		
		Connection conn = null; 
        PreparedStatement myStmt = null;
        //String query = "INSERT INTO opis (oid,okod,fond,oname,g3,g4,g13,g14,g24) VALUES (?,?,?,?,?,?,?,?,?)";
        String query = "UPDATE opis SET okod = ?, oname = ?, g3 = ?, g4 = ?, g13 = ?, g14 = ?, g24 = ? WHERE oid = ?";

       try{
    	   conn = DriverManager.getConnection(dataSourceProperties.getUrl());
           myStmt = conn.prepareStatement(query);
           
           //myStmt.setString(1,obj.getOid());
           myStmt.setString(1,obj.getOkod());
           myStmt.setString(2,obj.getOname());
           myStmt.setInt(3,obj.getG3());
           myStmt.setInt(4,obj.getG4());
           myStmt.setInt(5,obj.getG13());
           myStmt.setString(6,obj.getG14());
           myStmt.setInt(7,obj.getG24());
           myStmt.setString(8,obj.getOid());
          
           myStmt.executeUpdate();
           conn.close();
           myStmt.close();
        
       }catch (Exception e){
    	   e.printStackTrace();
       }finally {
           try {
               if (myStmt != null) myStmt.close();
               if (conn != null) conn.close();
           } catch (SQLException e) {
               e.printStackTrace();
           }
           }
	}

}
