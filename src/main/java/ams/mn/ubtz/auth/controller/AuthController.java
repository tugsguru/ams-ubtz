package ams.mn.ubtz.auth.controller;

import ams.mn.ubtz.auth.dto.LoginRequest;
import ams.mn.ubtz.auth.dto.LoginResponse;
import ams.mn.ubtz.auth.dto.RegisterRequest;
import ams.mn.ubtz.auth.dto.ResetPasswordRequest;
import ams.mn.ubtz.auth.service.UserService;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        boolean success = userService.register(request.getUname(), request.getPass(), request.getWname());

        if (success) {
            return ResponseEntity.ok("Бүртгэл амжилттай.");
        } else {
            return ResponseEntity.status(500).body("Бүртгэл амжилтгүй.");
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request.getUname(), request.getPass());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/admin/reset-password")
    public ResponseEntity<?> resetPasswordByAdmin(@RequestBody ResetPasswordRequest request) {
        boolean success = userService.resetPasswordByAdmin(
            request.getAdminUname(), 
            request.getTargetUname(), 
            request.getNewPassword()
        );

        if (success) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Нууц үг амжилттай шинэчлэгдлээ.");
            response.put("success", true);
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Админ эрх байхгүй эсвэл хэрэглэгч олдсонгүй.");
            response.put("success", false);
            return ResponseEntity.status(403).body(response);
        }
    }


    /*@PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        boolean success = userService.login(request.getUname(), request.getPass());

        if (success) {
            return ResponseEntity.ok("Нэвтрэлт амжилттай.");
        } else {
            return ResponseEntity.status(401).body("Нэвтрэх нэр эсвэл нууц үг буруу.");
        }
    }*/
}
