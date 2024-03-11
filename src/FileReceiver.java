import java.io.*;
import java.net.Socket;
import java.util.Map;

public class FileReceiver implements Runnable {
    private final Socket socket;
    private final Map<String, FileMetadata> files;

    public FileReceiver(Socket socket, Map<String, FileMetadata> files) {
        this.socket = socket;
        this.files = files;
    }

    @Override
    public void run() {
        try (InputStream inputStream = socket.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String fileName = reader.readLine(); // First line is the file name
            FileMetadata metadata = files.get(fileName);
            if (metadata != null) {
                try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(metadata.getFileName()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.equals("EOF")) {
                            break; // End of file transfer
                        } else if (line.startsWith("Chunk:")) {
                            // Handle chunk metadata (if needed)
                            continue;
                        }
                        // Write chunk to file
                        byte[] buffer = line.getBytes();
                        bufferedOutputStream.write(buffer, 0, buffer.length);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
