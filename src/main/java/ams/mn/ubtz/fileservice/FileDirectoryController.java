package ams.mn.ubtz.fileservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/directory")
public class FileDirectoryController {
    
    @Value("${file.upload-dir:./uploads}")
    private String uploadDirectory;
    
    private final FileDirectoryService fileService;

    public FileDirectoryController(FileDirectoryService fileService) {
        this.fileService = fileService;
    }

    /**
     * GET /api/directory/info?path=/custom/path
     * Тодорхой хавтасны мэдээлэл. Path заагаагүй бол upload-dir ашиглана
     */
    @GetMapping("/info")
    public DirectoryInfo getDirectoryInfo(@RequestParam(required = false) String path) {
        try {
            String targetPath = (path != null && !path.isEmpty()) ? path : uploadDirectory;
            return fileService.getDirectoryInfo(targetPath);
        } catch (Exception e) {
            DirectoryInfo errorInfo = new DirectoryInfo();
            errorInfo.setPath(path != null ? path : uploadDirectory);
            errorInfo.setError(e.getMessage());
            return errorInfo;
        }
    }

    /**
     * GET /api/directory/upload-dir
     * file.upload-dir хавтасны мэдээлэл (D:/amsdata/MTZ)
     */
    @GetMapping("/upload-dir")
    public DirectoryInfo getUploadDirectoryInfo() {
        return getDirectoryInfo(null);
    }
}
