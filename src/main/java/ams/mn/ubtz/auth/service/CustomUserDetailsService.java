package ams.mn.ubtz.auth.service;

import ams.mn.ubtz.auth.model.User;
import ams.mn.ubtz.config.DataSourceProperties;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.*;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final DataSourceProperties dataSourceProperties;
    private final PasswordEncoder passwordEncoder;

    public CustomUserDetailsService(DataSourceProperties dataSourceProperties, PasswordEncoder passwordEncoder) {
        this.dataSourceProperties = dataSourceProperties;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String sql = "SELECT uid, uname, pass, permission, wname FROM users WHERE uname = ?";

        try (Connection conn = DriverManager.getConnection(dataSourceProperties.getUrl());
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUid(rs.getInt("uid"));
                user.setUname(rs.getString("uname"));
                user.setPass(rs.getString("pass"));
                user.setPermission(rs.getInt("permission"));
                user.setWname(rs.getString("wname"));

                // ➕ Шинэ нэмэлт: нууц үг хэшлэгдсэн эсэхийг шалгаж, хэрвээ хэшлэгдээгүй байвал автоматаар BCrypt болгоно
                String rawPassword = user.getPass();

                // Хэрвээ аль хэдийнээ BCrypt бол биш бол
                if (!rawPassword.startsWith("$2a$") && !rawPassword.startsWith("$2b$") && !rawPassword.startsWith("$2y$")) {
                    String encodedPassword = passwordEncoder.encode(rawPassword);

                    // ➕ UPDATE query ашиглан hashed password-ийг хадгалах
                    try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE users SET pass = ? WHERE uid = ?")) {
                        updateStmt.setString(1, encodedPassword);
                        updateStmt.setInt(2, user.getUid());
                        updateStmt.executeUpdate();
                        user.setPass(encodedPassword); // Hashed version-г буцаах
                    }
                }

                return new UserDetailsImpl(user);

            } else {
                throw new UsernameNotFoundException("Хэрэглэгч олдсонгүй: " + username);
            }
        } catch (SQLException e) {
            throw new UsernameNotFoundException("Өгөгдлийн санд алдаа гарлаа", e);
        }
    }
}
