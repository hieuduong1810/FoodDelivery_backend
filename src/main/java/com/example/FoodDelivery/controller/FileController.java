package com.example.FoodDelivery.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.FoodDelivery.domain.res.file.ResUploadFileDTO;
import com.example.FoodDelivery.service.FileService;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.StorageException;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1")
public class FileController {
    private final FileService fileService;

    @Value("${foodDelivery.upload-file.base-uri}")
    private String baseURI;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/files")
    @ApiMessage("Upload single file")
    public ResponseEntity<ResUploadFileDTO> updateFile(
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam(name = "folder", required = false) String folder)
            throws URISyntaxException, IOException, StorageException {
        // skip validate
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
        this.fileService.createDirectory(baseURI + folder);

        // store file
        String uploadedFileName = this.fileService.store(file, folder);

        ResUploadFileDTO res = new ResUploadFileDTO(uploadedFileName, Instant.now());

        return ResponseEntity.ok().body(res);
    }

    // @GetMapping("/files")
    // @ApiMessage("Download file")
    // public ResponseEntity<Resource> download(@RequestParam(name = "fileName",
    // required = false) String fileName,
    // @RequestParam(name = "folder", required = false) String folder)
    // throws URISyntaxException, IOException, StorageException {
    // if (fileName == null || folder == null) {
    // throw new StorageException("File name or folder is missing.");
    // }

    // // check file exist (and not a directory)
    // long fileLength = this.fileService.getFileLength(fileName, folder);
    // if (fileLength == 0) {
    // throw new StorageException("File does not exist: " + fileName);
    // }

    // // download file
    // InputStreamResource resource = this.fileService.getResource(fileName,
    // folder);

    // return ResponseEntity.ok()
    // .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName
    // + "\"")
    // .contentLength(fileLength)
    // .contentType(MediaType.APPLICATION_OCTET_STREAM)
    // .body(resource);
    // }
}
