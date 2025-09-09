package ams.mn.ubtz.fileservice;

public class DirectoryInfo {
    private String path;
    private int fileCount = 0;
    private long totalSizeBytes = 0;
    private String formattedSize;
    private String error;
    private long timestamp;

    public DirectoryInfo() {
        this.timestamp = System.currentTimeMillis();
        this.formattedSize = "0 B";
    }

    // Getters and Setters
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    
    public int getFileCount() { return fileCount; }
    public void setFileCount(int fileCount) { this.fileCount = fileCount; }
    public void incrementFileCount() { 
        this.fileCount++;
        updateFormattedSize();
    }
    
    public long getTotalSizeBytes() { return totalSizeBytes; }
    public void setTotalSizeBytes(long totalSizeBytes) { 
        this.totalSizeBytes = totalSizeBytes; 
        updateFormattedSize();
    }
    public void addToTotalSize(long size) { 
        this.totalSizeBytes += size;
        updateFormattedSize();
    }
    
    public String getFormattedSize() { return formattedSize; }
    public void setFormattedSize(String formattedSize) { this.formattedSize = formattedSize; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    // Хэмжээг форматлах
    private void updateFormattedSize() {
        if (totalSizeBytes < 1024) {
            this.formattedSize = totalSizeBytes + " B";
        } else if (totalSizeBytes < 1024 * 1024) {
            this.formattedSize = String.format("%.2f KB", totalSizeBytes / 1024.0);
        } else if (totalSizeBytes < 1024 * 1024 * 1024) {
            this.formattedSize = String.format("%.2f MB", totalSizeBytes / (1024.0 * 1024));
        } else {
            this.formattedSize = String.format("%.2f GB", totalSizeBytes / (1024.0 * 1024 * 1024));
        }
    }
}