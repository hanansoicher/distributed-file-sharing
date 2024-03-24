import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class CLI {
    private static Scanner scanner = new Scanner(System.in);
    private static Peer peer;

    public static void main(String[] args) {
        System.out.println("Welcome to the Distributed File Sharing System");

        int m = 4; // Number of bits in the identifier space
        int nodeId = promptForNodeId();
        peer = new Peer(nodeId, m);
        peer.joinNetwork(null); // Join the network (null indicates first node)

        while (true) {
            showMenu();
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    uploadFile();
                    break;
                case 2:
                    searchFile();
                    break;
                case 3:
                    downloadFile();
                    break;
                case 4:
                    listFiles();
                    break;
                case 5:
                    shareFile();
                    break;
                case 6:
                    listSharedFiles();
                    break;
                case 7:
                    leaveNetwork();
                    return; // Exit the program
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void showMenu() {
        System.out.println("\nPlease choose an action:");
        System.out.println("1. Upload a file");
        System.out.println("2. Search for a file");
        System.out.println("3. Download a file");
        System.out.println("4. List available files");
        System.out.println("5. Share a file with a user");
        System.out.println("6. List files shared with me");
        System.out.println("7. Leave the network and exit");
    }

    private static void uploadFile() {
        System.out.print("Enter the path of the file to upload: ");
        String filePath = scanner.nextLine();
        File file = new File(filePath);

        if (file.exists() && file.isFile()) {
            String fileName = file.getName();
            long fileSize = file.length(); // Automatically detect the file size

            // Store the file metadata
            peer.storeFile(fileName, fileSize, filePath);

            System.out.println("File '" + fileName + "' uploaded successfully.");
        } else {
            System.out.println("File not found or is not a file: " + filePath);
        }
    }



    private static void searchFile() {
        System.out.print("Enter the file name to search: ");
        String fileName = scanner.nextLine();

        FileMetadata metadata = peer.retrieveFile(fileName);
        if (metadata != null) {
            System.out.println("File found: " + metadata);
        } else {
            System.out.println("File '" + fileName + "' not found in the network.");
        }
    }

    private static void downloadFile() {
        System.out.print("Enter the file name to download: ");
        String fileName = scanner.nextLine();

        FileMetadata metadata = peer.retrieveFile(fileName);
        if (metadata != null) {
            try (Socket socket = new Socket(metadata.getOwnerAddress(), peer.getNode().getFileServerPort())) {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(fileName);  // Send the file name to the server
                peer.getNode().receiveFileData(fileName, socket);  // Receive the file data
                System.out.println("File downloaded: " + fileName);
            } catch (IOException e) {
                System.out.println("Error downloading file: " + e.getMessage());
            }
        } else {
            System.out.println("File not found in the network.");
        }
    }


    private static void listFiles() {
        List<String> fileNames = peer.getStoredFileNames();
        if (fileNames.isEmpty()) {
            System.out.println("No files available in the network.");
        } else {
            System.out.println("Available files:");
            for (String fileName : fileNames) {
                System.out.println("- " + fileName);
            }
        }
    }

    private static void shareFile() {
        System.out.print("Enter the file name to share: ");
        String fileName = scanner.nextLine();
        System.out.print("Enter the IP address of the user to share with: ");
        String userIpAddress = scanner.nextLine();

        peer.shareFile(fileName, userIpAddress);
    }

    private static void listSharedFiles() {
        List<String> sharedFiles = peer.getFilesSharedWithMe();
        if (sharedFiles.isEmpty()) {
            System.out.println("No files have been shared with you.");
        } else {
            System.out.println("Files shared with you:");
            for (String fileName : sharedFiles) {
                System.out.println("- " + fileName);
            }
        }
    }

    private static void leaveNetwork() {
        System.out.println("Leaving the network...");
        peer.leaveNetwork();
    }

    private static int promptForNodeId() {
        System.out.print("Enter your node ID: ");
        return scanner.nextInt();
    }
}
