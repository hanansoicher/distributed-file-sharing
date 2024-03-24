import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int m = 4; // Number of bits in the identifier space

        // Start and join nodes to the network
        Peer peer1 = new Peer(1, m);
        peer1.joinNetwork(null); // First peer in the network

        Thread.sleep(1000); // Wait for the first peer to stabilize

        Peer peer2 = new Peer(2, m);
        peer2.joinNetwork(peer1); // Second peer joins the network

        Thread.sleep(1000); // Wait for the network to stabilize

        Peer peer3 = new Peer(3, m);
        peer3.joinNetwork(peer1); // Third peer joins the network

        Thread.sleep(1000); // Wait for the network to stabilize

        // Store files in the network
        peer1.storeFile("topic10", 1000, "/Users/hanansoicher/Downloads/Topic 10_100a.pdf");
        peer2.storeFile("topic9.txt", 2000, "/Users/hanansoicher/Downloads/Topic 9_100a.pdf");
        peer3.storeFile("file3.txt", 3000, "path/to/file3.txt");

        // Retrieve and print file metadata
        FileMetadata file1Metadata = peer2.retrieveFile("file1.txt");
        FileMetadata file2Metadata = peer3.retrieveFile("file2.txt");
        FileMetadata file3Metadata = peer1.retrieveFile("file3.txt");

        System.out.println(file1Metadata);
        System.out.println(file2Metadata);
        System.out.println(file3Metadata);

        // Share files
        peer1.shareFile("file1.txt", peer2.getNode().getIpAddress());
        peer2.shareFile("file2.txt", peer3.getNode().getIpAddress());

        // List files shared with peer3
        List<String> sharedFiles = peer3.getFilesSharedWithMe();
        System.out.println("Files shared with Peer 3:");
        for (String fileName : sharedFiles) {
            System.out.println(fileName);
        }

        // Delete a file from the network
        peer1.deleteFile("file1.txt");

        // List available files after deletion
        List<String> availableFiles = peer2.getStoredFileNames();
        System.out.println("Available files after deletion:");
        for (String fileName : availableFiles) {
            System.out.println(fileName);
        }

        // A peer leaves the network
        peer3.leaveNetwork();
    }
}
