package ams.mn.ubtz.doc.service.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import java.nio.file.*;

import ams.mn.ubtz.doc.service.FileSyncService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileSyncServiceImpl implements FileSyncService {

    // Configuration properties
    @Value("${remote.api.url}")
    private String remoteApiUrl;
    @Value("${file.download.base.url}")
    private String fileBaseUrl;
    @Value("${file.save.directory}")
    private String saveDirectory;
    @Value("${download.thread-pool-size:10}")
    private int threadPoolSize;
    @Value("${download.max-retry:3}")
    private int maxRetry;
    @Value("${download.connect-timeout:5000}")
    private int connectTimeout;
    @Value("${download.read-timeout:30000}")
    private int readTimeout;
    @Value("${download.large-file.size-threshold:10485760}") // Default 10MB
    private String sizeThreshold;
    @Value("${download.large-file.thread-pool:5}")
    private int largeFileThreadPoolSize;
    @Value("${download.state.db:download_state.db}")
    private String stateDbPath;
    
    private static final Logger log = LoggerFactory.getLogger(FileSyncServiceImpl.class);

    @Autowired
    private RestTemplate restTemplate;

    // Execution services
    private long largeFileSizeThreshold;
    private ExecutorService executor;
    private ExecutorService executorLargeFiles;
    
    // Нийт татагдсан файлын хэмжээг (байтаар) хадгалах
    private AtomicLong totalDownloadedBytes = new AtomicLong(0);
    
    // State tracking
    private ConcurrentMap<String, DownloadStatus> fileStatusMap = new ConcurrentHashMap<>();
    private AtomicInteger successCount = new AtomicInteger(0);
    private AtomicInteger totalCount = new AtomicInteger(0);
    private Set<String> downloadedFiles = ConcurrentHashMap.newKeySet();
    private List<String> failedFiles = Collections.synchronizedList(new ArrayList<>());

    enum DownloadStatus {
        PENDING,
        DOWNLOADING,
        COMPLETED,
        FAILED
    }

    @PostConstruct
    public void init() {
        this.largeFileSizeThreshold = parseSize(sizeThreshold);
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
        this.executorLargeFiles = Executors.newFixedThreadPool(largeFileThreadPoolSize);
        loadDownloadState();
        startFileSystemWatcher();
        log.info("Initialized executor services - Main: {}, Large Files: {}", 
                threadPoolSize, largeFileThreadPoolSize);
    }

    private long parseSize(String size) {
        String cleanSize = size.split("#")[0].trim();
        if (cleanSize.endsWith("MB")) {
            return Long.parseLong(cleanSize.replace("MB", "").trim()) * 1024 * 1024;
        } else if (cleanSize.endsWith("KB")) {
            return Long.parseLong(cleanSize.replace("KB", "").trim()) * 1024;
        }
        return Long.parseLong(cleanSize);
    }

    @Override
    public SyncResult syncFilesFromRemote() {
        int page = 0;
        boolean hasMore = true;

        try {
            while (hasMore && !Thread.currentThread().isInterrupted()) {
                String apiUrl = String.format("%s/list?page=%d&size=%d", remoteApiUrl, page, 1000);
                
                try {
                    ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                        apiUrl,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<Map<String, Object>>>() {}
                    );

                    if (!response.getStatusCode().is2xxSuccessful()) {
                        log.error("API returned status code: {}", response.getStatusCode());
                        hasMore = false;
                        break;
                    }

                    List<Map<String, Object>> dataList = response.getBody();
                    if (dataList == null || dataList.isEmpty()) {
                        hasMore = false;
                        log.info("No more data. Total pages processed: {}", page);
                        break;
                    }

                    CountDownLatch pageLatch = new CountDownLatch(dataList.size());

                    for (Map<String, Object> row : dataList) {
                        executor.submit(() -> {
                            try {
                                processRow(row);
                            } finally {
                                pageLatch.countDown();
                            }
                        });
                    }

                    try {
                        if (!pageLatch.await(1, TimeUnit.HOURS)) {
                            log.warn("Page processing timeout");
                            break;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new ServiceInterruptedException("Processing interrupted", e);
                    }

                    page++;
                    log.info("Processed page {}. Success: {}, Failed: {}, Total: {}",
                        page, successCount.get(), failedFiles.size(), totalCount.get());

                } catch (ResourceAccessException e) {
                    log.error("Network error processing page {}: {}", page, e.getMessage());
                    hasMore = false;
                } catch (Exception e) {
                    log.error("Error processing page {}: {}", page, e.getMessage(), e);
                    hasMore = false;
                }
            }
        } finally {
            // Бодит файлын тоог шалгах
            long actualFileCount = 0;
            try {
                actualFileCount = Files.list(Paths.get(saveDirectory))
                                     .filter(p -> !p.getFileName().toString().endsWith(".tmp"))
                                     .filter(p -> {
                                         return Files.isRegularFile(p);
                                     })
                                     .count();
            } catch (IOException e) {
                log.error("Error listing files in directory: {}", saveDirectory, e);
            }
            
            log.info("Validation: Actual files={}, Reported success={}", 
                    actualFileCount, successCount.get());
            
            if (successCount.get() != actualFileCount) {
                log.warn("Discrepancy detected! Adjusting counts... Difference: {}", 
                        Math.abs(successCount.get() - actualFileCount));
                successCount.set((int)actualFileCount);
            }
            
            verifyDownloadCounts();
            saveDownloadState();
            cleanup();
        }

        logFailedDownloads(failedFiles);
        
     // Энд тайлангийн мэдээллийг нэмнэ
        log.info("Download Summary: Total={}, Success={} ({}%), Failed={}, TotalSize={}, AvgSize={}",
            totalCount.get(),
            successCount.get(),
            String.format("%.1f", totalCount.get() > 0 ? (successCount.get() * 100.0 / totalCount.get()) : 0),
            failedFiles.size(),
            formatFileSize(totalDownloadedBytes.get()),
            formatFileSize(totalCount.get() > 0 ? (totalDownloadedBytes.get() / totalCount.get()) : 0)
        );
        
        return new SyncResult(totalCount.get(), successCount.get(), 
                failedFiles.size(), totalDownloadedBytes.get());
    }
    
    // Хэмжээг форматлаж харуулах туслах функц
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        String[] units = {"KB", "MB", "GB", "TB"};
        int unitIndex = (int) (Math.log(bytes) / Math.log(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, unitIndex), units[unitIndex-1]);
    }

    /*private void processRow(Map<String, Object> row) {
        if (row == null) {
            log.warn("Null row encountered");
            return;
        }

        String fileName = (String) row.get("t9");
        if (fileName == null || fileName.trim().isEmpty()) {
            log.debug("Empty filename in row: {}", row);
            return;
        }

        totalCount.incrementAndGet();
        if (!downloadedFiles.add(fileName)) {
           
        	if (Files.exists(Paths.get(saveDirectory, fileName))) {
                // Зөвхөн шинээр татагдсан биш, өмнө нь байсан файлыг тоолохгүй
                log.debug("File already existed: {}", fileName);
            } else {
                log.warn("File marked as downloaded but missing: {}", fileName);
                downloadedFiles.remove(fileName);  // Давхардсан бүртгэлийг арилгах
            }
            return;
        }

        String fileUrl = fileBaseUrl + fileName;
        fileStatusMap.put(fileName, DownloadStatus.DOWNLOADING);
        
        try {
            if (isLargeFile(fileName)) {
                executorLargeFiles.submit(() -> handleFileDownload(fileUrl, fileName, true));
            } else {
                executor.submit(() -> handleFileDownload(fileUrl, fileName, false));
            }
        } catch (RejectedExecutionException e) {
            fileStatusMap.put(fileName, DownloadStatus.FAILED);
            failedFiles.add(fileName + " | Error: Thread pool overload");
            log.error("Thread pool busy for file: {}", fileName);
        }
    }*/
    private void processRow(Map<String, Object> row) {
        if (row == null) {
            log.warn("Null row encountered");
            return;
        }

        String fileName = (String) row.get("t9");
        if (fileName == null || fileName.trim().isEmpty()) {
            log.debug("Empty filename in row: {}", row);
            return;
        }

        totalCount.incrementAndGet();

        Path filePath = Paths.get(saveDirectory, fileName);

        synchronized (downloadedFiles) {
            if (downloadedFiles.contains(fileName)) {
                if (!Files.exists(filePath)) {
                    log.warn("Duplicate file name in memory but missing on disk: {}", fileName);
                    downloadedFiles.remove(fileName);
                } else {
                    log.debug("Skipping already processed file: {}", fileName);
                    return;
                }
            }
        }

        String fileUrl = fileBaseUrl + fileName;
        fileStatusMap.put(fileName, DownloadStatus.DOWNLOADING);

        boolean submitted = false;

        try {
            Runnable task = () -> handleFileDownload(fileUrl, fileName, isLargeFile(fileName));
            if (isLargeFile(fileName)) {
                executorLargeFiles.submit(task);
            } else {
                executor.submit(task);
            }
            submitted = true;
        } catch (RejectedExecutionException e) {
            fileStatusMap.put(fileName, DownloadStatus.FAILED);
            failedFiles.add(fileName + " | Error: Thread pool overload");
            log.error("Thread pool busy for file: {}", fileName);
        }

        if (submitted) {
            synchronized (downloadedFiles) {
                downloadedFiles.add(fileName); // зөвхөн амжилттай submit хийсний дараа л бүртгэнэ
            }
        }
    }


    /*private void handleFileDownload(String fileUrl, String fileName, boolean isLargeFile) {
        for (int i = 0; i < maxRetry; i++) {
            try {
                if (isLargeFile) {
                    downloadLargeFile(fileUrl, fileName);
                } else {
                    downloadFile(fileUrl, fileName);
                }

                if (verifyFileCompletion(fileName)) {
                    fileStatusMap.put(fileName, DownloadStatus.COMPLETED);

                    // Шалгаж байж successCount тоолно
                    Path path = Paths.get(saveDirectory, fileName);
                    if (Files.exists(path) && Files.size(path) > 0 && !fileName.endsWith(".tmp")) {
                        successCount.incrementAndGet();
                    }

                    log.debug("Downloaded {} ({})", fileName, isLargeFile ? "large" : "small");
                    break;
                }

            } catch (IOException e) {
                if (i == maxRetry - 1) {
                    fileStatusMap.put(fileName, DownloadStatus.FAILED);
                    failedFiles.add(fileName + " | Error: " + e.getMessage());
                    log.error("Failed after {} retries: {}", maxRetry, fileName, e);
                } else {
                    try {
                        Thread.sleep(calculateBackoffDelay(i));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
    }*/
    private void handleFileDownload(String fileUrl, String fileName, boolean isLargeFile) {
        for (int i = 0; i < maxRetry; i++) {
            try {
                if (isLargeFile) {
                    downloadLargeFile(fileUrl, fileName);
                } else {
                    downloadFile(fileUrl, fileName);
                }

                if (verifyFileCompletion(fileName)) {
                    fileStatusMap.put(fileName, DownloadStatus.COMPLETED);

                    Path filePath = Paths.get(saveDirectory, fileName);
                    if (Files.exists(filePath) && Files.size(filePath) > 0) {
                        successCount.incrementAndGet();
                        log.trace("✅ Counted as success: {}", fileName);
                    }

                    break;
                }

            } catch (IOException e) {
                if (i == maxRetry - 1) {
                    fileStatusMap.put(fileName, DownloadStatus.FAILED);
                    failedFiles.add(fileName + " | Error: " + e.getMessage());
                    log.error("Failed after {} retries: {}", maxRetry, fileName, e);
                } else {
                    try {
                        Thread.sleep(calculateBackoffDelay(i));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
    }



    private void downloadFile(String fileUrl, String fileName) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "FileDownloader/2.0");

            int responseCode = connection.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
            	
            	long fileSize = connection.getContentLengthLong();
                totalDownloadedBytes.addAndGet(fileSize);
                
                Path targetPath = Paths.get(saveDirectory).resolve(fileName);
                Files.createDirectories(targetPath.getParent());

                try (InputStream in = new BufferedInputStream(connection.getInputStream());
                     OutputStream out = new BufferedOutputStream(Files.newOutputStream(targetPath))) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    out.flush();
                }

                verifyFileSize(connection, targetPath);
            } else {
                throw new HttpStatusCodeException(responseCode, fileUrl);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void downloadLargeFile(String fileUrl, String fileName) throws IOException {
        long startTime = System.currentTimeMillis();
        HttpURLConnection connection = null;
        Path tempPath = Paths.get(saveDirectory, fileName + ".tmp");
        Path finalPath = Paths.get(saveDirectory, fileName);
        
        try {
            connection = (HttpURLConnection) new URL(fileUrl).openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(300000);
            
            long fileSize = connection.getContentLengthLong();
            totalDownloadedBytes.addAndGet(fileSize);
            
            log.info("Starting large file download: {} ({} MB)", 
                fileName, fileSize / (1024 * 1024));

            try (InputStream in = new BufferedInputStream(connection.getInputStream());
                 OutputStream out = new BufferedOutputStream(Files.newOutputStream(tempPath))) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalRead = 0;
                
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                    
                    if (totalRead % (5 * 1024 * 1024) == 0) {
                        log.debug("Download progress: {} - {}%", 
                            fileName, (int)((totalRead * 100) / fileSize));
                    }
                }
            }
            
            Files.move(tempPath, finalPath, StandardCopyOption.ATOMIC_MOVE);
            log.info("Completed large file: {} ({} ms)", 
                fileName, System.currentTimeMillis() - startTime);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            Files.deleteIfExists(tempPath);
        }
    }

    /*private boolean verifyFileCompletion(String fileName) {
        Path filePath = Paths.get(saveDirectory, fileName);

        try {
            boolean exists = Files.exists(filePath);
            boolean notEmpty = exists && Files.size(filePath) > 0;
            boolean readable = exists && Files.isReadable(filePath);

            // .tmp файл байвал бүрэн татагдаагүй гэж үзнэ
            if (fileName.endsWith(".tmp")) {
                log.warn("File still has .tmp extension: {}", fileName);
                return false;
            }

            // Нэр нь зөв боловч файл байхгүй бол
            if (!exists) {
                log.warn("File not found after supposed download: {}", fileName);
                return false;
            }

            if (!notEmpty) {
                log.warn("Empty file detected: {}", fileName);
                Files.deleteIfExists(filePath);  // Цэвэрлэж орхи
                return false;
            }

            return readable;

        } catch (IOException e) {
            log.error("Verification failed for {}: {}", fileName, e.getMessage());
            return false;
        }
    }*/
    private boolean verifyFileCompletion(String fileName) {
        Path filePath = Paths.get(saveDirectory, fileName);
        try {
            if (fileName.endsWith(".tmp")) {
                log.warn("File has .tmp extension: {}", fileName);
                return false;
            }

            if (!Files.exists(filePath)) {
                log.warn("File not found: {}", fileName);
                return false;
            }

            long size = Files.size(filePath);
            if (size == 0) {
                log.warn("File is empty: {}", fileName);
                Files.deleteIfExists(filePath);  // цэвэрлэнэ
                return false;
            }

            if (!Files.isReadable(filePath)) {
                log.warn("File is not readable: {}", fileName);
                return false;
            }

            return true;

        } catch (IOException e) {
            log.error("Verification error for {}: {}", fileName, e.getMessage());
            return false;
        }
    }



    private void verifyFileSize(HttpURLConnection connection, Path filePath) throws IOException {
        long expectedSize = connection.getContentLengthLong();
        if (expectedSize > 0 && Files.size(filePath) != expectedSize) {
            Files.deleteIfExists(filePath);
            throw new IOException("File size mismatch. Expected: " + expectedSize + 
                               ", Actual: " + Files.size(filePath));
        }
    }

    private boolean isLargeFile(String fileName) {
        return fileName.matches(".*\\.(zip|rar|iso|tar\\.gz|mp4|avi)$") || 
               fileName.contains("large_") ||
               fileName.startsWith("bigfile_");
    }

    private long calculateBackoffDelay(int retryCount) {
        return (long) Math.min(1000 * Math.pow(2, retryCount), 30000);
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (executor != null) {
                log.info("Shutting down main executor service");
                shutdownExecutor(executor);
            }
            
            if (executorLargeFiles != null) {
                log.info("Shutting down large files executor service");
                shutdownExecutor(executorLargeFiles);
            }
        } catch (Exception e) {
            log.error("Error during executor shutdown", e);
        }
    }

    private void shutdownExecutor(ExecutorService executorService) {
        if (executorService == null) return;
        
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {  // 30 сек болгож багасгав
                executorService.shutdownNow();
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    log.error("Executor did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void logFailedDownloads(List<String> failedFiles) {
        if (!failedFiles.isEmpty()) {
            try {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                Path logFile = Paths.get(saveDirectory, "failed_downloads_" + timestamp + ".log");
                
                List<String> enhancedLogs = failedFiles.stream()
                    .map(f -> String.format("[%s] %s", timestamp, f))
                    .collect(Collectors.toList());
                
                Files.write(logFile, enhancedLogs, StandardOpenOption.CREATE);
                log.error("{} files failed to download. See {}", failedFiles.size(), logFile);
            } catch (IOException e) {
                log.error("Failed to save error log", e);
            }
        }
    }

    private void verifyDownloadCounts() {
        try {
            long actualFiles = Files.list(Paths.get(saveDirectory))
                                  .filter(p -> {
                                      try {
                                          return Files.isRegularFile(p) && 
                                                 Files.size(p) > 0;
                                      } catch (IOException e) {
                                          return false;
                                      }
                                  })
                                  .count();
            
            long trackedSuccess = fileStatusMap.values().stream()
                                           .filter(s -> s == DownloadStatus.COMPLETED)
                                           .count();
            
            log.info("Final Verification:");
            log.info("Tracked Success: {}", trackedSuccess);
            log.info("Actual Files: {}", actualFiles);
            
            if (trackedSuccess != actualFiles) {
                log.warn("Discrepancy detected! Difference: {}", Math.abs(trackedSuccess - actualFiles));
                
                fileStatusMap.forEach((name, status) -> {
                    if (status == DownloadStatus.COMPLETED && 
                        !Files.exists(Paths.get(saveDirectory, name))) {
                        log.warn("Missing completed file: {}", name);
                    }
                });
            }
        } catch (IOException e) {
            log.error("Verification failed", e);
        }
    }

    private void startFileSystemWatcher() {
        executor.submit(() -> {
            WatchService watchService = null;
            try {
                watchService = FileSystems.getDefault().newWatchService();
                Path dirPath = Paths.get(saveDirectory);
                
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                    log.info("Directory created: {}", dirPath);
                }
                
                dirPath.register(watchService, 
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
                
                log.info("Started watching directory: {}", dirPath);
                
                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key;
                    try {
                        key = watchService.take();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        
                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            log.warn("Events may have been lost");
                            continue;
                        }
                        
                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> ev = (WatchEvent<Path>)event;
                        Path filename = ev.context();
                        Path child = dirPath.resolve(filename);
                        
                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            log.info("File created: {}", child);
                        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            log.warn("File deleted: {}", child);
                        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            log.debug("File modified: {}", child);
                        }
                    }
                    
                    if (!key.reset()) {
                        log.warn("WatchKey no longer valid");
                        break;
                    }
                }
            } catch (ClosedWatchServiceException e) {
                log.info("WatchService was closed");
            } catch (Exception e) {
                log.error("File watcher error", e);
            } finally {
                if (watchService != null) {
                    try {
                        watchService.close();
                    } catch (IOException e) {
                        log.error("Error closing WatchService", e);
                    }
                }
                log.info("Stopped watching directory");
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void loadDownloadState() {
        if (Files.exists(Paths.get(stateDbPath))) {
            try (ObjectInputStream ois = new ObjectInputStream(
                Files.newInputStream(Paths.get(stateDbPath)))) {
                fileStatusMap = (ConcurrentMap<String, DownloadStatus>)ois.readObject();
                log.info("Loaded previous download state with {} entries", fileStatusMap.size());
            } catch (Exception e) {
                log.error("Failed to load download state", e);
            }
        }
    }

    private void saveDownloadState() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
            Files.newOutputStream(Paths.get(stateDbPath)))) {
            oos.writeObject(fileStatusMap);
            log.debug("Saved download state with {} entries", fileStatusMap.size());
        } catch (IOException e) {
            log.error("Failed to save download state", e);
        }
    }

    private static class HttpStatusCodeException extends IOException {
        private final int statusCode;

        public HttpStatusCodeException(int statusCode, String url) {
            super("HTTP error " + statusCode + " for URL: " + url);
            this.statusCode = statusCode;
        }
    }

    private static class ServiceInterruptedException extends RuntimeException {
        public ServiceInterruptedException(String message, InterruptedException cause) {
            super(message, cause);
        }
    }

    public static class SyncResult {
        private final int total;
        private final int success;
        private final int failed;
        private final long totalSizeBytes;

        public SyncResult(int total, int success, int failed, long totalSizeBytes) {
            this.total = total;
            this.success = success;
            this.failed = failed;
            this.totalSizeBytes = totalSizeBytes;
        }
        
        // Format хийх метод
        public String getFormattedTotalSize() {
            if (totalSizeBytes < 1024) return totalSizeBytes + " B";
            int exp = (int)(Math.log(totalSizeBytes)/Math.log(1024));
            String units = "KMGTPE".charAt(exp-1) + "B";
            return String.format("%.2f %s", totalSizeBytes/Math.pow(1024, exp), units);
        }

        public int getTotal() { return total; }
        public int getSuccess() { return success; }
        public int getFailed() { return failed; }

        public double getSuccessRate() {
            return total > 0 ? (success * 100.0 / total) : 0;
        }
    }
}