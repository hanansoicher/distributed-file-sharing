import java.util.ArrayList;
import java.util.List;

public class Peer {
    private Node node;
    private DHT dht;

    public Peer(int id, int m) {
        this.node = new Node(id, m);
        this.dht = new DHT(m);
    }

    public void joinNetwork(Peer existingPeer) {
        if (existingPeer != null) {
            this.dht.addNode(this.node);
            this.node.join(this.dht, existingPeer.node);
        } else {
            // This peer is the first node in the network
            this.dht.addNode(this.node);
            this.node.join(this.dht, null);
        }
    }

    public void leaveNetwork() {
        this.dht.removeNode(this.node);
    }

    public void storeFile(String fileName, long fileSize, String filePath) {
        FileMetadata metadata = new FileMetadata(fileName, fileSize, this.node.getIpAddress(), this.dht.getM());
        metadata.setOwner(this.node.getIpAddress());
        metadata.setSharedWithUsers(new ArrayList<>());
        metadata.setFilePath(filePath); // Set the file path
        Node responsibleNode = this.dht.findSuccessor(metadata.getKey());
        responsibleNode.addFile(fileName, metadata);
        System.out.println("File '" + fileName + "' stored at node " + responsibleNode.getId());
    }

    public FileMetadata retrieveFile(String fileName) {
        int fileKey = HashingUtil.hash(fileName, this.dht.getM());
        Node responsibleNode = this.dht.findSuccessor(fileKey);
        FileMetadata metadata = responsibleNode.getFile(fileName);
        if (metadata != null) {
            System.out.println("File '" + fileName + "' retrieved from node " + responsibleNode.getId());
            return metadata;
        } else {
            System.out.println("File '" + fileName + "' not found in the network.");
            return null;
        }
    }

    public void shareFile(String fileName, String userIpAddress) {
        FileMetadata metadata = this.node.getFile(fileName);
        if (metadata != null && metadata.getOwner().equals(this.node.getIpAddress())) {
            metadata.getSharedWithUsers().add(userIpAddress);
            System.out.println("File '" + fileName + "' shared with user " + userIpAddress);
        } else {
            System.out.println("File not found or you are not the owner of the file.");
        }
    }

    public List<String> getFilesSharedWithMe() {
        List<String> sharedFiles = new ArrayList<>();
        for (FileMetadata metadata : this.node.getFiles().values()) {
            if (metadata.getSharedWithUsers().contains(this.node.getIpAddress())) {
                sharedFiles.add(metadata.getFileName());
            }
        }
        return sharedFiles;
    }


    public void deleteFile(String fileName) {
        int fileKey = HashingUtil.hash(fileName, this.dht.getM());
        Node responsibleNode = this.dht.findSuccessor(fileKey);
        responsibleNode.removeFile(fileName);
        System.out.println("File '" + fileName + "' deleted from node " + responsibleNode.getId());
    }

    public List<String> getStoredFileNames() {
        return node.getFileNames();
    }

    public Node getNode() {
        return node;
    }
}
