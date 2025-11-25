package com.example.FoodDelivery.controller;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.FoodDelivery.domain.res.file.ResUploadFileDTO;
import com.example.FoodDelivery.service.CloudinaryService;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.StorageException;

@RestController
@RequestMapping("/api/v1")
public class FileController {
    private final CloudinaryService cloudinaryService;

    public FileController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping("/files")
    @ApiMessage("Upload single file")
    public ResponseEntity<ResUploadFileDTO> uploadFile(
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam(name = "folder", required = false) String folder)
            throws StorageException {
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new StorageException("File is empty. Please upload a valid file.");
        }

        String fileName = file.getOriginalFilename();
        List<String> allowedExtensions = Arrays.asList("pdf", "jpg", "jpeg", "png", "doc", "docx");
        boolean isValid = allowedExtensions.stream()
                .anyMatch(item -> fileName.toLowerCase().endsWith("." + item));

        if (!isValid) {
            throw new StorageException("Invalid file type. Allowed types are: " + String.join(", ", allowedExtensions));
        }

        // Use "general" as default folder if not provided
        if (folder == null || folder.trim().isEmpty()) {
            folder = "general";
        }

        // Upload to Cloudinary
        String uploadedFileUrl = this.cloudinaryService.uploadFile(file, folder);

        ResUploadFileDTO res = new ResUploadFileDTO(uploadedFileUrl, Instant.now());

        return ResponseEntity.ok().body(res);
    }
}
