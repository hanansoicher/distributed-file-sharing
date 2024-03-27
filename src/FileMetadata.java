import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileMetadata {
    private String fileName;
    private long fileSize;
    private int key;
    private final LocalDateTime creationDate;
    private String filePath;
    private String ownerUserId;
    private List<String> sharedWithUserIds;

    public FileMetadata(String fileName, long fileSize, int m, String ownerUserId) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.key = HashingUtil.hash(fileName, m);
        this.creationDate = LocalDateTime.now();
        this.ownerUserId = ownerUserId;
        this.sharedWithUserIds = new ArrayList<>();
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

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setSharedWithUserIds(List<String> sharedWithUserIds) {
        this.sharedWithUserIds = sharedWithUserIds;
    }

    public List<String> getSharedWithUserIds() {
        return sharedWithUserIds;
    }

    public void shareWithUser(String userId) {
        if (!sharedWithUserIds.contains(userId)) {
            sharedWithUserIds.add(userId);
        }
    }

    @Override
    public String toString() {
        return "FileMetadata{" +
                "fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", key=" + key +
                ", ownerUserId='" + ownerUserId + '\'' +
                ", creationDate=" + creationDate +
                '}';
    }
}
