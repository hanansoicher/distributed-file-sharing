import java.util.List;

public class Main {
    public static void main(String[] args) {
        int m = 4; // Number of bits in the identifier space

        // Create peers
        Peer peer1 = new Peer(1, m);
        Peer peer2 = new Peer(2, m);
        Peer peer3 = new Peer(3, m);

        // Start and join nodes to the network in separate threads
        new Thread(() -> {
            peer1.joinNetwork(null); // First peer in the network
            System.out.println("Peer 1 joined the network.");
        }).start();

        // Wait for a short period to allow the first peer to stabilize
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            peer2.joinNetwork(peer1); // Second peer joins the network
            System.out.println("Peer 2 joined the network.");
        }).start();

        new Thread(() -> {
            peer3.joinNetwork(peer1); // Third peer joins the network
            System.out.println("Peer 3 joined the network.");
        }).start();

        // Wait for the network to stabilize before proceeding with file operations
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Store files in the network
        peer1.storeFile("topic10.pdf", "/Users/hanansoicher/Downloads/Topic 10_100a.pdf");
        peer2.storeFile("topic9.pdf", "/Users/hanansoicher/Downloads/Topic 9_100a.pdf");
        peer3.storeFile("topic7.pdf", "/Users/hanansoicher/Downloads/Topic 7_100a.pdf");

        // Retrieve and print file metadata
        FileMetadata file1Metadata = peer2.retrieveFile("topic10.pdf");
        FileMetadata file2Metadata = peer3.retrieveFile("topic9.pdf");
        FileMetadata file3Metadata = peer1.retrieveFile("topic7.pdf");

        System.out.println(file1Metadata);
        System.out.println(file2Metadata);
        System.out.println(file3Metadata);

        // Share files
        peer1.shareFile("topic10.pdf", peer2.getNode().getIpAddress());
        peer2.shareFile("topic9.pdf", peer3.getNode().getIpAddress());

        // List files shared with peer3
        List<String> sharedFiles = peer3.getFilesSharedWithMe();
        System.out.println("Files shared with Peer 3:");
        for (String fileName : sharedFiles) {
            System.out.println(fileName);
        }

        // Delete a file from the network
        peer1.deleteFile("topic10.pdf");

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
