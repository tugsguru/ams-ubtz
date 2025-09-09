package ams.mn.ubtz.auth.service;

import ams.mn.ubtz.auth.dto.LoginResponse;
import ams.mn.ubtz.config.DataSourceProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class UserService {

    private final DataSourceProperties dataSourceProperties;
	
	public UserService(DataSourceProperties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    /*@Autowired
    private BCryptPasswordEncoder passwordEncoder;
    */
	
    @Autowired
    private PasswordEncoder passwordEncoder; // 🔐 Энэ нь MixedPasswordEncoder-г inject хийнэ

    // Хэрэглэгч бүртгэх
    public boolean register(String uname, String rawPassword, String wname) {
        
    	String sql = "INSERT INTO users (uname, pass, permission, wname) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dataSourceProperties.getUrl());
             PreparedStatement stmt = conn.prepareStatement(sql);
        ) {

            String hashedPassword = passwordEncoder.encode(rawPassword);
            stmt.setString(1, uname);
            stmt.setString(2, hashedPassword);
            stmt.setInt(3, 1); // permission 1 гэж онооно
            stmt.setString(4, wname);

            int rowCount = stmt.executeUpdate();
            return rowCount == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public LoginResponse login(String uname, String rawPassword) {
        String sql = "SELECT uid, uname, pass, wname, permission FROM users WHERE uname = ?";
        String updateSql = "UPDATE users SET pass = ? WHERE uname = ?";

        try (Connection conn = DriverManager.getConnection(dataSourceProperties.getUrl());
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, uname);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("pass");
                boolean isBCrypt = storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$");

                boolean matched = false;
                if (isBCrypt) {
                    matched = passwordEncoder.matches(rawPassword, storedPassword);
                } else {
                    if (storedPassword.equals(rawPassword)) {
                        String hashed = passwordEncoder.encode(rawPassword);
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setString(1, hashed);
                            updateStmt.setString(2, uname);
                            updateStmt.executeUpdate();
                        }
                        matched = true;
                    }
                }

                if (matched) {
                    int uid = rs.getInt("uid");
                    String wname = rs.getString("wname");
                    int permission = rs.getInt("permission");

                    return new LoginResponse(uid, uname, wname, permission, true);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Амжилтгүй бол
        return new LoginResponse(0, uname, null, 0, false);
    }
    
    //Админ хэрэглэгч бусдын нууц үгийг солих
    public boolean resetPasswordByAdmin(String adminUname, String targetUname, String newRawPassword) {
        String sql = "SELECT permission FROM users WHERE uname = ?";

        try (Connection conn = DriverManager.getConnection(dataSourceProperties.getUrl());
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, adminUname);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int permission = rs.getInt("permission");
                if (permission == 1) {
                    // 🔐 Админ бол шинэ нууц үгийг BCrypt хийнэ
                    String hashed = passwordEncoder.encode(newRawPassword);
                    updatePassword(targetUname, hashed);
                    return true;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    
    private void updatePassword(String uname, String newHashedPassword) {
        
    	String sql = "UPDATE users SET pass = ? WHERE uname = ?";

        try (Connection conn = DriverManager.getConnection(dataSourceProperties.getUrl());
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newHashedPassword);
            stmt.setString(2, uname);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
