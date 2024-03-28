package com.dfs.distributedfilesharing;

import com.dfs.distributedfilesharing.entity.FileMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FileController {
    private final FileService fileService;
    private final String uploadDirectory = "uploads/";

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("ownerUsername") String ownerUsername) {
        try {
            String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDirectory + uniqueFileName);
            Files.write(path, file.getBytes());

            FileMetadata metadata = new FileMetadata(uniqueFileName, file.getSize(), 4, ownerUsername);
            fileService.saveFileMetadata(metadata);

            return ResponseEntity.ok("File uploaded successfully: " + uniqueFileName);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Could not upload the file: " + file.getOriginalFilename());
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            FileMetadata metadata = fileService.getFileMetadata(filename);
            if (metadata != null) {
                Path path = Paths.get(metadata.getFilePath());
                Resource resource = new UrlResource(path.toUri());
                String contentType = Files.probeContentType(path);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getFileName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileMetadata>> listFiles() {
        List<FileMetadata> files = fileService.getAllFilesMetadata();
        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/delete/{filename}")
    public ResponseEntity<String> deleteFile(@PathVariable String filename) {
        if (fileService.deleteFileMetadata(filename)) {
            return ResponseEntity.ok("File deleted successfully: " + filename);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
