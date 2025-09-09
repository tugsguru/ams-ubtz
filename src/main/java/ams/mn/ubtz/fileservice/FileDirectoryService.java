package ams.mn.ubtz.fileservice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

@Service
public class FileDirectoryService {

    /**
     * Тодорхой хавтасны файлын тоо, нийт хэмжээг тооцоолох
     */
    public DirectoryInfo getDirectoryInfo(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            
            if (!Files.exists(path) || !Files.isDirectory(path)) {
                throw new IllegalArgumentException("Directory does not exist: " + directoryPath);
            }

            DirectoryInfo info = new DirectoryInfo();
            info.setPath(directoryPath);
            
            // Файлын тоо болон хэмжээ тооцоолох
            try (Stream<Path> fileStream = Files.walk(path)) {
                fileStream
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            info.incrementFileCount();
                            info.addToTotalSize(Files.size(file));
                        } catch (IOException e) {
                            System.err.println("Error getting size for file: " + file + " - " + e.getMessage());
                        }
                    });
            }
            
            return info;
            
        } catch (IOException e) {
            throw new RuntimeException("Error reading directory: " + directoryPath, e);
        }
    }
}