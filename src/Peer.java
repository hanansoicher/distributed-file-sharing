import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Peer {
    private Node node;
    private DHT dht;
    private String userId;

    public Peer(int id, int m, String userId) {
        this.node = new Node(id, m);
        this.dht = new DHT(m);
        this.userId = userId;
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
            long fileSize = file.length();
            FileMetadata metadata = new FileMetadata(fileName, fileSize, this.dht.getM(), this.userId);
            metadata.setFilePath(filePath);
            Node responsibleNode = this.dht.findSuccessor(metadata.getKey());
            responsibleNode.addFile(fileName, metadata, this.userId);
            System.out.println("File '" + fileName + "' stored at node " + responsibleNode.getId());
        } else {
            System.out.println("File not found or is not a file: " + filePath);
        }
    }

    public FileMetadata retrieveFile(String fileName) {
        int fileKey = HashingUtil.hash(fileName, this.dht.getM());
        Node responsibleNode = this.dht.findSuccessor(fileKey);
        FileMetadata metadata = responsibleNode.getFile(fileName, this.userId);
        if (metadata != null) {
            System.out.println("File '" + fileName + "' retrieved from node " + responsibleNode.getId());
            return metadata;
        } else {
            System.out.println("File '" + fileName + "' not found in the network.");
            return null;
        }
    }

    public void shareFile(String fileName, String userToShareWith) {
        FileMetadata metadata = this.node.getFile(fileName, this.userId);
        if (metadata != null && metadata.getOwnerUserId().equals(this.userId)) {
            metadata.shareWithUser(userToShareWith);
            System.out.println("File '" + fileName + "' shared with user " + userToShareWith);
        } else {
            System.out.println("File not found or you are not the owner of the file.");
        }
    }

    public List<String> getFilesSharedWithMe() {
        List<String> sharedFiles = new ArrayList<>();
        for (FileMetadata metadata : this.node.getFiles().values()) {
            if (metadata.getSharedWithUserIds().contains(this.userId)) {
                sharedFiles.add(metadata.getFileName());
            }
        }
        return sharedFiles;
    }

    public void deleteFile(String fileName) {
        int fileKey = HashingUtil.hash(fileName, this.dht.getM());
        Node responsibleNode = this.dht.findSuccessor(fileKey);
        responsibleNode.removeFile(fileName, this.userId);
        System.out.println("File '" + fileName + "' deleted from node " + responsibleNode.getId());
    }

    public List<String> getStoredFileNames() {
        return node.getFileNames();
    }

    public List<FileMetadata> searchFiles(String searchQuery) {
        List<FileMetadata> results = new ArrayList<>();
        for (FileMetadata metadata : node.getFiles().values()) {
            if (metadata.getFileName().contains(searchQuery) && (metadata.getOwnerUserId().equals(this.userId) || metadata.getSharedWithUserIds().contains(this.userId))) {
                results.add(metadata);
            }
        }
        return results;
    }

    public Node getNode() {
        return node;
    }
}
