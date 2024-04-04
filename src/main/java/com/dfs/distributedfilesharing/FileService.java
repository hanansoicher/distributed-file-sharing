package com.dfs.distributedfilesharing;

import com.dfs.distributedfilesharing.entity.FileMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileService {
    private final FileMetadataRepository fileMetadataRepository;

    @Autowired
    public FileService(FileMetadataRepository fileMetadataRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
    }

    public void saveFileMetadata(FileMetadata fileMetadata) {
        fileMetadataRepository.save(fileMetadata);
    }

    public FileMetadata getFileMetadata(String filename) {
        return fileMetadataRepository.findByFileName(filename);
    }

    public List<FileMetadata> getAllFilesMetadata() {
        return fileMetadataRepository.findAll();
    }

    public List<FileMetadata> searchFiles(String query) {
        return fileMetadataRepository.findAll().stream()
                .filter(file -> file.getFileName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<FileMetadata> getFilesForUser(String username) {
        return fileMetadataRepository.findAll().stream()
                .filter(file -> file.getOwnerUsername().equals(username) || file.getSharedWithUsernames().contains(username))
                .collect(Collectors.toList());
    }

    public List<FileMetadata> searchFilesForUser(String query, String username) {
        return fileMetadataRepository.findAll().stream()
                .filter(file -> (file.getFileName().toLowerCase().contains(query.toLowerCase())
                        && (file.getOwnerUsername().equals(username) || file.getSharedWithUsernames().contains(username))))
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean deleteFileMetadata(String filename) {
        FileMetadata fileMetadata = fileMetadataRepository.findByFileName(filename);
        if (fileMetadata != null) {
            try {
                // Delete the file from the file system
                Path path = Paths.get(fileMetadata.getFilePath());
                Files.delete(path);
                // Delete the file metadata from the database
                fileMetadataRepository.delete(fileMetadata);
                return true;
            } catch (IOException e) {
                // Log the error or handle it appropriately
                return false;
            }
        } else {
            return false;
        }
    }
}
