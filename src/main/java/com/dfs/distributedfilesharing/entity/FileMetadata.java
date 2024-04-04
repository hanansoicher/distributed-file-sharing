package com.dfs.distributedfilesharing.entity;

import com.dfs.distributedfilesharing.HashingUtil;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;
    private long fileSize;
    private int file_key;
    private final LocalDateTime creationDate;
    private String filePath;
    private String ownerUsername;
    private List<String> sharedWithUsernames = new ArrayList<>();

    public FileMetadata() {
        this.creationDate = LocalDateTime.now();
    }

    public FileMetadata(String fileName, long fileSize, int m, String ownerUsername) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.file_key = HashingUtil.hash(fileName, m);
        this.creationDate = LocalDateTime.now();
        this.ownerUsername = ownerUsername;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFilePath() { return filePath; }

    public void setFilePath(String filePath) { this.filePath = filePath; }

    public int getFileKey() {
        return file_key;
    }

    public void setFileKey(int file_key) {
        this.file_key = file_key;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setSharedWithUsernames(List<String> sharedWithUsernames) {
        this.sharedWithUsernames = sharedWithUsernames;
    }

    public List<String> getSharedWithUsernames() {
        return sharedWithUsernames;
    }

    public void shareWithUsername(String ownerUsername) {
        if (!sharedWithUsernames.contains(ownerUsername)) {
            sharedWithUsernames.add(ownerUsername);
        }
    }

    @Override
    public String toString() {
        return "FileMetadata{" +
                "fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", file_key=" + file_key +
                ", ownerUsername='" + ownerUsername + '\'' +
                ", creationDate=" + creationDate +
                '}';
    }
}
