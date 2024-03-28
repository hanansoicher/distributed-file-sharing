package com.dfs.distributedfilesharing;

import com.dfs.distributedfilesharing.entity.FileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class Peer {
    private static final Logger logger = LoggerFactory.getLogger(Peer.class);
    private final Node node;
    private final DHT dht;
    private final FileService fileService;
    private final String Username;

    @Autowired
    public Peer(Node node, DHT dht, FileService fileService) {
        this.node = node;
        this.dht = dht;
        this.fileService = fileService;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = "none";
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        }
        this.Username = username;
    }

    public void joinNetwork(Peer existingPeer) {
        if (existingPeer != null) {
            this.dht.addNode(this.node);
            this.node.join(this.dht, existingPeer.node);
        } else {
            this.dht.addNode(this.node);
            this.node.join(this.dht, null);
        }
    }

    public void leaveNetwork() {
        this.dht.removeNode(this.node);
    }

    public void storeFile(String fileName, String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            FileMetadata metadata = createFileMetadata(fileName, file.length(), filePath);
            fileService.saveFileMetadata(metadata);
            logger.info("File '{}' stored successfully.", fileName);
        } else {
            logger.error("File not found or is not a file: {}", filePath);
        }
    }

    private FileMetadata createFileMetadata(String fileName, long fileSize, String filePath) {
        FileMetadata metadata = new FileMetadata(fileName, fileSize, this.dht.getM(), this.Username);
        metadata.setFilePath(filePath);
        return metadata;
    }

    public FileMetadata retrieveFile(String fileName) {
        return fileService.getFileMetadata(fileName);
    }

    public void shareFile(String fileName, String userToShareWith) {
        FileMetadata metadata = fileService.getFileMetadata(fileName);
        if (metadata != null && metadata.getOwnerUsername().equals(this.Username)) {
            metadata.shareWithUsername(userToShareWith);
            fileService.saveFileMetadata(metadata);
            logger.info("File '" + fileName + "' shared with user " + userToShareWith);
        } else {
            logger.error("File not found or you are not the owner of the file.");
        }
    }

    public List<String> getFilesSharedWithMe() {
        return fileService.getAllFilesMetadata().stream()
                .filter(metadata -> metadata.getSharedWithUsernames().contains(this.Username))
                .map(FileMetadata::getFileName)
                .collect(Collectors.toList());
    }

    public void downloadFile(String fileName) {
        FileMetadata metadata = retrieveFile(fileName);
        if (metadata != null) {
            node.downloadFile(fileName); // Download file directly from the Node
            logger.info("File downloaded: " + fileName);
        } else {
            logger.error("File not found in the network.");
        }
    }

    public void deleteFile(String fileName) {
        fileService.deleteFileMetadata(fileName);
        logger.info("File '" + fileName + "' deleted successfully.");
    }

    public List<String> getStoredFileNames() {
        return fileService.getAllFilesMetadata().stream()
                .map(FileMetadata::getFileName)
                .collect(Collectors.toList());
    }

    public List<FileMetadata> searchFiles(String searchQuery) {
        return fileService.getAllFilesMetadata().stream()
                .filter(metadata -> metadata.getFileName().contains(searchQuery))
                .collect(Collectors.toList());
    }

    public Node getNode() {
        return node;
    }

    public DHT getDht() {
        return dht;
    }

    public String getUsername() {
        return Username;
    }
}
