package ams.mn.ubtz.fond.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ams.mn.ubtz.fond.entity.Fond;
import ams.mn.ubtz.fond.service.FondService;


@Service("FondService")
public class FondServiceImpl implements FondService{
	
	 @Value("${spring.datasource.url}")
	 private String dsUrl;
	 
	 @Value("${spring.datasource.driver-class-name}")
	 private String driver;

	@Override
	public List<Object> getAllUsers() {
		
		List<Object> list = new ArrayList<>();
        String sql ="SELECT fid,fkod,a1,a3,a4,a7,a8,ft5.name FROM Fond,ft5 where fond.a12=ft5.ftid order by convert(int,fkod)"; 

        try (
        	 Connection conn = DriverManager.getConnection(dsUrl);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("fid", rs.getString("fid"));
                row.put("fkod", rs.getString("fkod"));
                row.put("a1", rs.getString("a1"));
                row.put("a3", rs.getInt("a3"));
                row.put("a4", rs.getInt("a4"));
                row.put("a7", rs.getString("a7"));
                row.put("a8", rs.getString("a8"));
                row.put("name", rs.getString("name"));
                list.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
	}
	

	@Override
	public void fondInsert(Fond obj) {
		
		Connection conn = null; 
        PreparedStatement myStmt = null;
        String query = "INSERT INTO fond (fid,fkod,fname,a1,a2,a3,a7,a8,a12) VALUES (?, ?, ?, ?, ?, ?,?,?,?)";

       try{
    	   conn = DriverManager.getConnection(dsUrl);
           myStmt = conn.prepareStatement(query);
           
           myStmt.setString(1,obj.getFid());
           myStmt.setString(2,obj.getFkod());
           myStmt.setString(3,obj.getFname());
           myStmt.setString(4,obj.getA1());
           myStmt.setInt(5,obj.getA2());
           myStmt.setInt(6,obj.getA3());
           myStmt.setString(7,obj.getA7());
           myStmt.setString(8,obj.getA8());
           myStmt.setInt(9,obj.getA12());
          
           myStmt.executeUpdate();
           conn.close();
           
           myStmt.close();
        
       }catch (Exception e){
    	   e.printStackTrace();
       }
		
	}

	@Override
	public List<Object> getSelect(String fid) {
		
		List<Object> list = new ArrayList<>();
        String sql ="SELECT fid,fkod,a1,a3,a4,a7,a8,a12 FROM Fond where fid = '"+ fid +"'";

        try (
        	 Connection conn = DriverManager.getConnection(dsUrl);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("fid", rs.getString("fid"));
                row.put("fkod", rs.getString("fkod"));
                row.put("a1", rs.getString("a1"));
                row.put("a3", rs.getInt("a3"));
                row.put("a4", rs.getInt("a4"));
                row.put("a7", rs.getString("a7"));
                row.put("a8", rs.getString("a8"));
                row.put("a12", rs.getString("a12"));
                list.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
	}

	@Override
	public void fondUpdate(Fond obj) {
		
		Connection conn = null;
	    PreparedStatement myStmt = null;

	    String query = "UPDATE fond SET fkod = ?, fname = ?, a1 = ?, a2 = ?, a3 = ?, a7 = ?, a8 = ?, a12 = ? WHERE fid = ?";

	    try {
	        conn = DriverManager.getConnection(dsUrl);
	        myStmt = conn.prepareStatement(query);

	        myStmt.setString(1, obj.getFkod());
	        myStmt.setString(2, obj.getFname());
	        myStmt.setString(3, obj.getA1());
	        myStmt.setInt(4, obj.getA2());
	        myStmt.setInt(5, obj.getA3());
	        myStmt.setString(6, obj.getA7());
	        myStmt.setString(7, obj.getA8());
	        myStmt.setInt(8, obj.getA12());
	        myStmt.setString(9, obj.getFid()); // WHERE нөхцөлд ашиглах fid

	        myStmt.executeUpdate();
	        conn.close();
	        myStmt.close();

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}


	@Override
	public List<Object> getFid(String fkod) {
		List<Object> list = new ArrayList<>();
        String sql ="SELECT fid,fkod,a1 FROM Fond where fkod = '"+ fkod +"'";

        try (
        	 Connection conn = DriverManager.getConnection(dsUrl);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("fid", rs.getString("fid"));
                row.put("fkod", rs.getString("fkod"));
                row.put("a1", rs.getString("a1"));
                list.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
	}

}
