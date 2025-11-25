package com.example.FoodDelivery.service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.FoodDelivery.util.error.StorageException;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Upload file to Cloudinary
     * 
     * @param file   - MultipartFile to upload
     * @param folder - folder name in Cloudinary (e.g., "dishes", "restaurants",
     *               "avatars")
     * @return URL of uploaded file
     * @throws StorageException if upload fails
     */
    public String uploadFile(MultipartFile file, String folder) throws StorageException {
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                throw new StorageException("File is empty. Please upload a valid file.");
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String publicId = UUID.randomUUID().toString();

            // Upload to Cloudinary with folder and public_id
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "public_id", publicId,
                            "resource_type", "auto"));

            // Return secure URL
            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            throw new StorageException("Failed to upload file to Cloudinary: " + e.getMessage());
        }
    }

    /**
     * Delete file from Cloudinary by public ID
     * 
     * @param publicId - the public ID of the file in Cloudinary (including folder
     *                 path)
     * @throws StorageException if deletion fails
     */
    public void deleteFile(String publicId) throws StorageException {
        try {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String resultStatus = (String) result.get("result");

            if (!"ok".equals(resultStatus) && !"not found".equals(resultStatus)) {
                throw new StorageException("Failed to delete file from Cloudinary");
            }
        } catch (IOException e) {
            throw new StorageException("Failed to delete file from Cloudinary: " + e.getMessage());
        }
    }

    /**
     * Extract public ID from Cloudinary URL
     * Example:
     * https://res.cloudinary.com/durzk8qz6/image/upload/v1234567890/dishes/uuid.jpg
     * Returns: dishes/uuid
     * 
     * @param cloudinaryUrl - full Cloudinary URL
     * @return public ID (folder/filename without extension)
     */
    public String extractPublicIdFromUrl(String cloudinaryUrl) {
        if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
            return null;
        }

        try {
            // Split by "/" and find the part after "upload/"
            String[] parts = cloudinaryUrl.split("/upload/");
            if (parts.length < 2) {
                return null;
            }

            // Get everything after version number (v1234567890/)
            String afterVersion = parts[1];
            String[] versionParts = afterVersion.split("/", 2);
            if (versionParts.length < 2) {
                return null;
            }

            // Remove file extension
            String publicIdWithExtension = versionParts[1];
            int lastDotIndex = publicIdWithExtension.lastIndexOf('.');
            if (lastDotIndex > 0) {
                return publicIdWithExtension.substring(0, lastDotIndex);
            }

            return publicIdWithExtension;
        } catch (Exception e) {
            return null;
        }
    }
}
