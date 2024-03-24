import java.io.*;
import java.net.Socket;
import java.util.Map;


public class FileReceiver implements Runnable {
    private final Socket socket;
    private final Map<String, FileMetadata> files;
    private final Node node;

    public FileReceiver(Socket socket, Map<String, FileMetadata> files, Node node) {
        this.socket = socket;
        this.files = files;
        this.node = node;
    }

    @Override
    public void run() {
        try {
            String fileName = receiveFileName(socket);
            if (fileName != null) {
                node.receiveFileData(fileName, socket);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String receiveFileName(Socket socket) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return reader.readLine();
    }
}
