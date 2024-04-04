package com.dfs.distributedfilesharing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.dfs.distributedfilesharing.entity.FileMetadata;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class Node {
    private final int id;
    private final int m; // Number of bits in identifier space
    private Node predecessor;
    private Node[] fingerTable;
    private Map<String, FileMetadata> files;
    private ServerSocket serverSocket;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private String ipAddress;
    private int fileServerPort;

    @Autowired
    private FileService fileService;

    public Node(@Value("${node.id}") int id, @Value("${node.m}") int m) {
        this.id = id;
        this.m = m;
        this.fingerTable = new Node[m];
        this.files = new HashMap<String, FileMetadata>();
        this.ipAddress = ipAddress;
        startFileServer();
        this.fileServerPort = serverSocket.getLocalPort();
    }

    public void addFile(String fileName, FileMetadata metadata, String Username) {
        if (Username.equals(metadata.getOwnerUsername())) {
            fileService.saveFileMetadata(metadata);
        } else {
            System.out.println("You do not have permission to add this file.");
        }
    }

    public void removeFile(String fileName, String Username) {
        FileMetadata metadata = files.get(fileName);
        if (metadata != null && Username.equals(metadata.getOwnerUsername())) {
            files.remove(fileName);
            fileService.deleteFileMetadata(metadata.getFileName());
        } else {
            System.out.println("You do not have permission to delete this file.");
        }
    }

    public FileMetadata getFile(String fileName, String Username) {
        FileMetadata metadata = files.get(fileName);
        if (metadata != null && (Username.equals(metadata.getOwnerUsername()) || metadata.getSharedWithUsernames().contains(Username))) {
            return metadata;
        } else {
            System.out.println("You do not have permission to access this file.");
            return null;
        }
    }

    public Map<String, FileMetadata> getFiles() {
        return files;
    }

    public void shareFile(String fileName, String Username, String userToShareWith) {
        FileMetadata metadata = files.get(fileName);
        if (metadata != null && Username.equals(metadata.getOwnerUsername())) {
            metadata.shareWithUsername(userToShareWith);
            fileService.saveFileMetadata(metadata);
        } else {
            System.out.println("You do not have permission to share this file.");
        }
    }

    public void startFileServer() {
        try {
            serverSocket = new ServerSocket(0); // Consider specifying a fixed port if needed
            System.out.println("File server started on port: " + serverSocket.getLocalPort());

            new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        FileReceiver fileReceiver = new FileReceiver(clientSocket, fileService, this);
                        new Thread(fileReceiver).start();
                    } catch (IOException e) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }).start();
        } catch (IOException e) {
            System.err.println("Error starting file server: " + e.getMessage());
        }
    }


    public void sendFile(String fileName, Node targetNode) {
        FileMetadata metadata = files.get(fileName);
        if (metadata != null) {
            int retries = 3;
            while (retries > 0) {
                try (Socket socket = new Socket(targetNode.getIpAddress(), targetNode.getFileServerPort());
                     FileInputStream fileInputStream = new FileInputStream(metadata.getFilePath());
                     BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                     OutputStream outputStream = socket.getOutputStream()) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    int chunkCount = 0;
                    while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                        String chunkMetadata = "Chunk:" + chunkCount + "\n";
                        outputStream.write(chunkMetadata.getBytes());
                        outputStream.write(buffer, 0, bytesRead);
                        chunkCount++;
                    }
                    String endOfFile = "EOF\n";
                    outputStream.write(endOfFile.getBytes());
                    break;
                } catch (IOException e) {
                    retries--;
                    if (retries == 0) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void sendFileData(String fileName, Socket targetSocket) {
        FileMetadata metadata = files.get(fileName);
        if (metadata != null) {
            try (FileInputStream fileInputStream = new FileInputStream(metadata.getFilePath());
                 BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                 OutputStream outputStream = targetSocket.getOutputStream()) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void receiveFileData(String fileName, Socket sourceSocket) {
        try (InputStream inputStream = sourceSocket.getInputStream();
             FileOutputStream fileOutputStream = new FileOutputStream("downloaded_" + fileName);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                bufferedOutputStream.write(buffer, 0, bytesRead);
            }
            System.out.println("File '" + fileName + "' has been downloaded successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error while downloading file '" + fileName + "': " + e.getMessage());
        }
    }

    public void downloadFile(String fileName) {
        FileMetadata metadata = files.get(fileName);
        if (metadata != null) {
            try (Socket socket = new Socket(metadata.getOwnerUsername(), fileServerPort)) {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(fileName);
                receiveFileData(fileName, socket);
            } catch (IOException e) {
                System.out.println("Error downloading file: " + e.getMessage());
            }
        } else {
            System.out.println("File not found in the network.");
        }
    }

    public List<String> getFileNames() {
        return new ArrayList<>(files.keySet());
    }

    private boolean isInRange(int key, int start, int end) {
        if (start < end) {
            return key > start && key < end;
        } else { // Range wraps around identifier space
            return key > start || key < end;
        }
    }

    public void updateFingerTable(DHT dht) {
        for (int i = 0; i < m; i++) {
            int start = (id + (int) Math.pow(2, i)) % (int) Math.pow(2, m);
            fingerTable[i] = dht.findSuccessor(start);
        }
    }

    public void join(DHT dht, Node existingNode) {
        if (existingNode != null) {
            initFingerTable(existingNode);
            updateOthers(dht);

            // Transfer files for which this node is now responsible
            Map<String, FileMetadata> transferredFiles = new HashMap<>();
            for (Map.Entry<String, FileMetadata> entry : predecessor.getFiles().entrySet()) {
                String fileName = entry.getKey();
                FileMetadata metadata = entry.getValue();
                int fileKey = metadata.getFileKey();
                if (isInRange(fileKey, id, predecessor.getId())) {
                    transferredFiles.put(fileName, metadata);
                    predecessor.removeFile(fileName, metadata.getOwnerUsername());
                }
            }
            files.putAll(transferredFiles);
        } else {
            // This node is first in the network
            for (int i = 0; i < m; i++) {
                fingerTable[i] = this;
            }
            predecessor = this;
        }
    }

    private void initFingerTable(Node existingNode) {
        fingerTable[0] = existingNode.findSuccessor((id + 1) % (int) Math.pow(2, m));
        predecessor = fingerTable[0].getPredecessor();
        fingerTable[0].setPredecessor(this);

        for (int i = 0; i < m - 1; i++) {
            if (isInRange((id + (int) Math.pow(2, i + 1)) % (int) Math.pow(2, m), id, fingerTable[i].getId())) {
                fingerTable[i + 1] = fingerTable[i];
            } else {
                fingerTable[i + 1] = existingNode.findSuccessor((id + (int) Math.pow(2, i + 1)) % (int) Math.pow(2, m));
            }
        }
    }

    private void updateOthers(DHT dht) {
        for (int i = 0; i < m; i++) {
            Node p = dht.findPredecessor((id - (int) Math.pow(2, i) + 1 + (int) Math.pow(2, m)) % (int) Math.pow(2, m));
            p.updateFingerTable(this, i);
        }
    }

    public void updateFingerTable(Node s, int i) {
        if (s.getId() != id && isInRange(s.getId(), id, fingerTable[i].getId())) {
            fingerTable[i] = s;
            Node p = predecessor;
            p.updateFingerTable(s, i);
        }
    }

    public void stabilize() {
        Node x = fingerTable[0].getPredecessor();
        if (x != null && isInRange(x.getId(), id, fingerTable[0].getId())) {
            fingerTable[0] = x;
        }
        fingerTable[0].notify(this);
    }

    public void startStabilization() {
        executorService.scheduleAtFixedRate(() -> {
            stabilize();
            fixFingers();
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void notify(Node n) {
        if (predecessor == null || isInRange(n.getId(), predecessor.getId(), id)) {
            predecessor = n;
        }
    }

    public void fixFingers() {
        for (int i = 0; i < m; i++) {
            fingerTable[i] = findSuccessor((id + (int) Math.pow(2, i)) % (int) Math.pow(2, m));
        }
    }

    public Node findSuccessor(int keyId) {
        if (isInRange(keyId, id, fingerTable[0].getId())) {
            return fingerTable[0];
        } else {
            Node node = closestPrecedingNode(keyId);
            return node.findSuccessor(keyId);
        }
    }

    public Node findSuccessor(String key) {
        int keyId = HashingUtil.hash(key, m);
        return findSuccessor(keyId);
    }

    public Node closestPrecedingNode(int keyId) {
        for (int i = m - 1; i >= 0; i--) {
            if (fingerTable[i] != null && isInRange(fingerTable[i].getId(), id, keyId)) {
                return fingerTable[i];
            }
        }
        return this;
    }

    public int getId() {
        return id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getFileServerPort() {
        return fileServerPort;
    }

    public Node getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(Node predecessor) {
        this.predecessor = predecessor;
    }

    public Node getSuccessor() {
        return fingerTable[0];
    }

    public void setSuccessor(Node successor) {
        fingerTable[0] = successor;
    }

    public void setFinger(int index, Node node) {
        fingerTable[index] = node;
    }
}