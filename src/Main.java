import java.util.List;

public class Main {
    public static void main(String[] args) {
        int m = 4;

        Peer peer1 = new Peer(1, m, "user1");
        Peer peer2 = new Peer(2, m, "user2");
        Peer peer3 = new Peer(3, m, "user3");

        new Thread(() -> {
            peer1.joinNetwork(null);
            System.out.println("Peer 1 joined the network.");
        }).start();

        // Wait to allow first peer to stabilize
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            peer2.joinNetwork(peer1);
            System.out.println("Peer 2 joined the network.");
        }).start();

        new Thread(() -> {
            peer3.joinNetwork(peer1);
            System.out.println("Peer 3 joined the network.");
        }).start();

        // Wait for the network to stabilize before proceeding with file operations
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Store files
        peer1.storeFile("topic10.pdf", "path/to/topic10.pdf");
        peer2.storeFile("topic9.pdf", "path/to/topic9.pdf");
        peer3.storeFile("topic7.pdf", "path/to/topic7.pdf");

        FileMetadata file1Metadata = peer2.retrieveFile("topic10.pdf");
        FileMetadata file2Metadata = peer3.retrieveFile("topic9.pdf");
        FileMetadata file3Metadata = peer1.retrieveFile("topic7.pdf");

        System.out.println(file1Metadata);
        System.out.println(file2Metadata);
        System.out.println(file3Metadata);

        peer1.shareFile("topic10.pdf", "user2");
        peer2.shareFile("topic9.pdf", "user3");

        List<String> sharedFiles = peer3.getFilesSharedWithMe();
        System.out.println("Files shared with Peer 3:");
        for (String fileName : sharedFiles) {
            System.out.println(fileName);
        }

        peer1.deleteFile("topic10.pdf");

        List<String> availableFiles = peer2.getStoredFileNames();
        System.out.println("Available files after deletion:");
        for (String fileName : availableFiles) {
            System.out.println(fileName);
        }

        peer3.leaveNetwork();
    }
}
