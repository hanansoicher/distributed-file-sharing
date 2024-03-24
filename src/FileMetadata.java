import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileMetadata {
    private String fileName;
    private long fileSize;
    private int key; // Unique identifier, could be a hash of the file content
    private String ownerAddress; // IP address or hostname of the owner node
    private final LocalDateTime creationDate;
    private List<String> sharedWithUsers;
    private String filePath;

    public FileMetadata(String fileName, long fileSize, String ownerAddress, int m) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.key = HashingUtil.hash(fileName, m);
        this.ownerAddress = ownerAddress;
        this.creationDate = LocalDateTime.now();
        this.sharedWithUsers = new ArrayList<>();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFilePath() { return filePath; }

    public void setFilePath(String filePath) { this.filePath = filePath; }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getOwnerAddress() {
        return ownerAddress;
    }

    public void setOwnerAddress(String ownerAddress) {
        this.ownerAddress = ownerAddress;
    }

    public List<String> getSharedWithUsers() { return sharedWithUsers; }

    public void setSharedWithUsers(List<String> sharedWithUsers) { this.sharedWithUsers = sharedWithUsers; }

    @Override
    public String toString() {
        return "FileMetadata{" +
                "fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", key=" + key +
                ", ownerAddress='" + ownerAddress + '\'' +
                ", creationDate=" + creationDate +
                '}';
    }
}
