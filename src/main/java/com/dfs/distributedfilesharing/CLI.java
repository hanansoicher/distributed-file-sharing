//package com.dfs.distributedfilesharing;
//
//import com.dfs.distributedfilesharing.entity.FileMetadata;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.io.File;
//import java.util.List;
//import java.util.Scanner;
//
//@Component
//public class CLI implements CommandLineRunner {
//    private final Peer peer;
//    private static final Scanner scanner = new Scanner(System.in);
//
//    public CLI(Peer peer) {
//        this.peer = peer;;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        // Your CLI logic here
//        System.out.println("Welcome to the Distributed File Sharing System");
//
//        int m = 4; // Number of bits in the identifier space
//        int nodeId = promptForNodeId();
//        String Username = promptForUsername();
//        peer = new Peer(nodeId, m, Username);
//        peer.joinNetwork(null);
//
//        while (true) {
//            showMenu();
//            int choice = scanner.nextInt();
//            scanner.nextLine(); // Consume newline
//
//            switch (choice) {
//                case 1:
//                    uploadFile();
//                    break;
//                case 2:
//                    searchFile();
//                    break;
//                case 3:
//                    downloadFile();
//                    break;
//                case 4:
//                    listFiles();
//                    break;
//                case 5:
//                    shareFile();
//                    break;
//                case 6:
//                    listSharedFiles();
//                    break;
//                case 7:
//                    leaveNetwork();
//                    return; // Exit the program
//                default:
//                    System.out.println("Invalid choice. Please try again.");
//            }
//        }
//    }
//
//    private static int promptForNodeId() {
//        System.out.print("Enter your node ID: ");
//        return scanner.nextInt();
//    }
//
//    private static String promptForUsername() {
//        System.out.print("Enter your username: ");
//        return scanner.nextLine();
//    }
//
//
//    private void showMenu() {
//        System.out.println("\nPlease choose an action:");
//        System.out.println("1. Upload a file");
//        System.out.println("2. Search for a file");
//        System.out.println("3. Download a file");
//        System.out.println("4. List available files");
//        System.out.println("5. Share a file with a user");
//        System.out.println("6. List files shared with me");
//        System.out.println("7. Leave the network and exit");
//    }
//
//
//
//    private void uploadFile() {
//        System.out.print("Enter the path of the file to upload: ");
//        String filePath = scanner.nextLine();
//        peer.storeFile(new File(filePath).getName(), filePath);
//        System.out.println("File uploaded successfully.");
//    }
//
//    private void searchFile() {
//        System.out.print("Enter the file name to search: ");
//        String fileName = scanner.nextLine();
//        List<FileMetadata> results = peer.searchFiles(fileName);
//        if (!results.isEmpty()) {
//            System.out.println("Search results:");
//            results.forEach(System.out::println);
//        } else {
//            System.out.println("No files found matching the search query.");
//        }
//    }
//
//    private void downloadFile() {
//        System.out.print("Enter the file name to download: ");
//        String fileName = scanner.nextLine();
//        peer.downloadFile(fileName);
//        System.out.println("File downloaded: " + fileName);
//    }
//
//    private void listFiles() {
//        List<String> fileNames = peer.getStoredFileNames();
//        if (fileNames.isEmpty()) {
//            System.out.println("No files available in the network.");
//        } else {
//            System.out.println("Available files:");
//            fileNames.forEach(fileName -> System.out.println("- " + fileName));
//        }
//    }
//
//    private void shareFile() {
//        System.out.print("Enter the file name to share: ");
//        String fileName = scanner.nextLine();
//        System.out.print("Enter the user ID of the user to share with: ");
//        String userToShareWith = scanner.nextLine();
//        peer.shareFile(fileName, userToShareWith);
//        System.out.println("File shared successfully.");
//    }
//
//    private void listSharedFiles() {
//        List<String> sharedFiles = peer.getFilesSharedWithMe();
//        if (sharedFiles.isEmpty()) {
//            System.out.println("No files have been shared with you.");
//        } else {
//            System.out.println("Files shared with you:");
//            sharedFiles.forEach(fileName -> System.out.println("- " + fileName));
//        }
//    }
//
//    private void leaveNetwork() {
//        System.out.println("Leaving the network...");
//        peer.leaveNetwork();
//    }
//}
