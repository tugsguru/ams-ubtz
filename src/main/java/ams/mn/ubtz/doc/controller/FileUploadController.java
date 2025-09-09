package ams.mn.ubtz.doc.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    // ams ubtz file upload
    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Хавтас байгаа эсэхийг шалгаад үүсгэнэ
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Файл хадгалах зам
            Path filePath = Paths.get(uploadDir, file.getOriginalFilename());
            file.transferTo(filePath.toFile());

            return ResponseEntity.ok("Амжилттай хадгалагдлаа: " + filePath);
            
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Файл хадгалах үед алдаа гарлаа: " + e.getMessage());
        }
    }
}
