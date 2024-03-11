public class Main {
    public static void main(String[] args) {
        int m = 4; // Number of bits in the identifier space

        // Create and join peers to the network
        Peer peer1 = new Peer(1, m);
        peer1.joinNetwork(null); // First peer in the network

        Peer peer2 = new Peer(2, m);
        peer2.joinNetwork(peer1); // Second peer joins the network

        Peer peer3 = new Peer(3, m);
        peer3.joinNetwork(peer1); // Third peer joins the network

        // Store some files in the network
        peer1.storeFile("file1.txt", 1000);
        peer2.storeFile("file2.txt", 2000);
        peer3.storeFile("file3.txt", 3000);

        // Retrieve files from the network
        FileMetadata file1Metadata = peer2.retrieveFile("file1.txt");
        FileMetadata file2Metadata = peer3.retrieveFile("file2.txt");
        FileMetadata file3Metadata = peer1.retrieveFile("file3.txt");

        // Print file metadata
        System.out.println(file1Metadata);
        System.out.println(file2Metadata);
        System.out.println(file3Metadata);

        // Delete a file from the network
        peer1.deleteFile("file1.txt");

        // A peer leaves the network
        peer3.leaveNetwork();

    }
}
