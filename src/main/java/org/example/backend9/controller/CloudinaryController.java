package org.example.backend9.controller;

import org.example.backend9.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
public class CloudinaryController {

    @Autowired
    private CloudinaryService cloudinaryService;

    /**
     * Endpoint upload ảnh
     * @param file MultipartFile từ client
     * @param folder folder lưu trên Cloudinary
     * @return URL ảnh đã upload
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "default") String folder
    ) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file, folder);
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload thất bại: " + e.getMessage());
        }
    }
}