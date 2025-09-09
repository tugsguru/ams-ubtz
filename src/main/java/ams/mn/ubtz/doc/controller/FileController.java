package ams.mn.ubtz.doc.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// call file without tomcat
@RestController
@RequestMapping("/data")
public class FileController {
	
    @Value("${file.upload-dir}")
    private String BASE_DIR;
    
    @Value("${file.save.directory}")
    private String BASE_DIR_ORG;    //dowloaded file location
    
    //private final String BASE_DIR = "D:/amsdata/MTZ"; // үндсэн хавтас
    //private final String BASE_DIR = "D:/archive/files";
    
    @GetMapping("/{fileName:.+}")
    public void getFile(
            @PathVariable String fileName,
            HttpServletResponse response) throws IOException {

        Path filePath = Paths.get(BASE_DIR, fileName);

        // Файл байгаа эсэхийг шалгах
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // MIME төрөл
        String mimeType = Files.probeContentType(filePath);
        if (mimeType == null) mimeType = "application/octet-stream";
        response.setContentType(mimeType);
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        response.setContentLengthLong(Files.size(filePath));

        // Stream дамжуулах
        try (InputStream in = Files.newInputStream(filePath);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[64 * 1024]; // 64KB buffer
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
    
    @GetMapping("/org/{fileName:.+}")
    public void getFileOrg(
            @PathVariable String fileName,
            HttpServletResponse response) throws IOException {

        Path filePath = Paths.get(BASE_DIR_ORG, fileName);

        // Файл байгаа эсэхийг шалгах
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // MIME төрөл
        String mimeType = Files.probeContentType(filePath);
        if (mimeType == null) mimeType = "application/octet-stream";
        response.setContentType(mimeType);
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        response.setContentLengthLong(Files.size(filePath));

        // Stream дамжуулах
        try (InputStream in = Files.newInputStream(filePath);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[64 * 1024]; // 64KB buffer
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}
