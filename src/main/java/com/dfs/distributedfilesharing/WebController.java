package com.dfs.distributedfilesharing;

import com.dfs.distributedfilesharing.entity.FileMetadata;
import com.dfs.distributedfilesharing.service.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class WebController {
    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

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

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @GetMapping("/files")
    public String files(Model model, Principal principal) {
        String username = principal.getName();
        List<FileMetadata> files = fileService.getFilesForUser(username);
        logger.info("Retrieved files for user '{}': {}", username, files);  // Add this logging statement
        model.addAttribute("files", files);
        return "files";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model, Principal principal) {
        try {
            String username = principal.getName();
            String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            String filePath = "uploads/" + username + "/" + uniqueFileName;
            Path path = Paths.get(filePath);
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            file.transferTo(path);
            peer.storeFile(uniqueFileName, filePath);
            model.addAttribute("success", "File uploaded successfully: " + uniqueFileName);
        } catch (IOException e) {
            String errorMessage = "Failed to upload file: " + e.getMessage();
            logger.error(errorMessage, e);
            model.addAttribute("error", errorMessage);
        }
        return "redirect:/files";
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, Principal principal) {
        String username = principal.getName();
        try {
            FileMetadata metadata = peer.retrieveFile(fileName);
            if (metadata != null && (username.equals(metadata.getOwnerUsername()) || metadata.getSharedWithUsernames().contains(username))) {
                Path path = Paths.get(metadata.getFilePath());
                Resource resource = new UrlResource(path.toUri());
                String contentType = Files.probeContentType(path);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            logger.error("Failed to download file: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/share")
    public ResponseEntity<String> shareFile(@RequestParam String fileName, @RequestParam String usernames, Principal principal) {
        String[] usernameArray = usernames.split(",");
        try {
            for (String username : usernameArray) {
                peer.shareFile(fileName, username.trim());
            }
            return ResponseEntity.ok("File shared successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to share file: " + e.getMessage());
        }
    }

    @GetMapping("/delete/{fileName}")
    public String deleteFile(@PathVariable String fileName, Model model) {
        try {
            peer.deleteFile(fileName);
            model.addAttribute("success", "File '" + fileName + "' deleted successfully.");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete file: " + e.getMessage());
        }
        return "redirect:/files";
    }

    @GetMapping("/search")
    public String searchFiles(@RequestParam String search, Principal principal, Model model) {
        String username = principal.getName();
        List<FileMetadata> results = fileService.searchFilesForUser(search, username);
        model.addAttribute("files", results);
        return "files";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }

}
