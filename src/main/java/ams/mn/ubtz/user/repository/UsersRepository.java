package ams.mn.ubtz.user.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import ams.mn.ubtz.user.model.Users;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

@Repository
public class UsersRepository {

	 @Value("${spring.datasource.url}")
	 private String dsUrl;
	 
	 @Value("${spring.datasource.driver-class-name}")
	 private String driver;
    
    /*public UsersRepository() {
    	
    	System.out.println(driver);
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        	//Class.forName(driver);
            
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQL Server драйвер олдсонгүй!", e);
        }
    }*/
    
    @PostConstruct
    public void init() {
        System.out.println("Injected driver value: " + driver);
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQL Server драйвер олдсонгүй!", e);
        }
    }

    public List<Users> findAll() {
        List<Users> users = new ArrayList<>();
        String sql = "SELECT uid, uname,pass,wname FROM users";

        try (//Connection conn = DriverManager.getConnection(url, username, password);
        	 Connection conn = DriverManager.getConnection(dsUrl);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(new Users(rs.getLong("uid"), rs.getString("uname"), rs.getString("pass"), rs.getString("wname")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

   /* public void save(Users user) {
        String sql = "INSERT INTO users (name) VALUES (?)";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }*/
}