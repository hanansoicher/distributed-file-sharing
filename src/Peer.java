import java.util.Map;

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

    public void storeFile(String fileName, long fileSize) {
        FileMetadata metadata = new FileMetadata(fileName, fileSize, this.node.getIpAddress(), this.dht.getM());
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

    public void deleteFile(String fileName) {
        int fileKey = HashingUtil.hash(fileName, this.dht.getM());
        Node responsibleNode = this.dht.findSuccessor(fileKey);
        responsibleNode.removeFile(fileName);
        System.out.println("File '" + fileName + "' deleted from node " + responsibleNode.getId());
    }

    // Additional methods for handling requests, etc.
}
