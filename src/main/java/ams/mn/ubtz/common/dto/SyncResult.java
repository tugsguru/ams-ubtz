package ams.mn.ubtz.common.dto;

import java.util.Objects;

public class SyncResult {
    private final int totalFiles;
    private final int successFiles;
    private final int failedFiles;

    public SyncResult(int totalFiles, int successFiles, int failedFiles) {
        this.totalFiles = totalFiles;
        this.successFiles = successFiles;
        this.failedFiles = failedFiles;
    }

    // Геттерүүд
    public int getTotalFiles() {
        return totalFiles;
    }

    public int getSuccessFiles() {
        return successFiles;
    }

    public int getFailedFiles() {
        return failedFiles;
    }

    // Тооцоолсон утга
    public double getSuccessRate() {
        return totalFiles > 0 ? 
               Math.round((successFiles * 100.0 / totalFiles) * 100) / 100.0 : 0.0;
    }

    // Equals & HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SyncResult that = (SyncResult) o;
        return totalFiles == that.totalFiles && 
               successFiles == that.successFiles && 
               failedFiles == that.failedFiles;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalFiles, successFiles, failedFiles);
    }

    // toString
    @Override
    public String toString() {
        return "SyncResult{" +
               "totalFiles=" + totalFiles +
               ", successFiles=" + successFiles +
               ", failedFiles=" + failedFiles +
               ", successRate=" + getSuccessRate() + "%" +
               '}';
    }

}
