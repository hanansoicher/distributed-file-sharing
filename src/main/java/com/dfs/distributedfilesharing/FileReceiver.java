package com.dfs.distributedfilesharing;

import com.dfs.distributedfilesharing.entity.FileMetadata;
import com.dfs.distributedfilesharing.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class FileReceiver implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(FileReceiver.class);
    private final Socket socket;
    private final FileService fileService;
    private final Node node;

    public FileReceiver(Socket socket, FileService fileService, Node node) {
        this.socket = socket;
        this.fileService = fileService;
        this.node = node;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String fileName = reader.readLine();
            if (fileName != null) {
                node.receiveFileData(fileName, socket);
            }
        } catch (IOException e) {
            logger.error("Error receiving file: {}", e.getMessage(), e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                logger.error("Error closing socket: {}", e.getMessage(), e);
            }
        }
    }
}
