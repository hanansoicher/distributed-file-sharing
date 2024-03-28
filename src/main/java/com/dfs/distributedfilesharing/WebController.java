package com.dfs.distributedfilesharing;

import com.dfs.distributedfilesharing.entity.FileMetadata;
import com.dfs.distributedfilesharing.entity.User;
import com.dfs.distributedfilesharing.FileService;
import com.dfs.distributedfilesharing.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class WebController {
    private final FileService fileService;
    private final Peer peer;
    private final UserDetailsServiceImpl userDetailsService;

    @Autowired
    public WebController(FileService fileService, Peer peer, UserDetailsServiceImpl userDetailsService) {
        this.fileService = fileService;
        this.peer = peer;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password) {
        userDetailsService.registerNewUserAccount(username, password);
        return "redirect:/login";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        userDetailsService.loadUserByUsername((username));
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/files")
    public String files(Model model) {
        List<FileMetadata> files = fileService.getAllFilesMetadata();
        model.addAttribute("files", files);
        return "files";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        try {
            String filePath = "uploads/" + file.getOriginalFilename();
            file.transferTo(Path.of(filePath));
            peer.storeFile(file.getOriginalFilename(), filePath);
        } catch (IOException e) {
            model.addAttribute("error", "Failed to upload file: " + e.getMessage());
            return "files";
        }
        return "redirect:/files";
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            FileMetadata metadata = peer.retrieveFile(fileName);
            if (metadata != null) {
                Path path = Paths.get(metadata.getFilePath());
                Resource resource = new UrlResource(path.toUri());
                String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/share/{fileName}")
    public String shareFile(@PathVariable String fileName, @RequestParam String Username) {
        peer.shareFile(fileName, Username);
        return "redirect:/files";
    }

    @GetMapping("/delete/{fileName}")
    public String deleteFile(@PathVariable String fileName) {
        peer.deleteFile(fileName);
        return "redirect:/files";
    }

    @GetMapping("/search")
    public String searchFiles(@RequestParam String search, Model model) {
        List<FileMetadata> results = fileService.searchFiles(search);
        model.addAttribute("files", results);
        return "files";
    }
}
