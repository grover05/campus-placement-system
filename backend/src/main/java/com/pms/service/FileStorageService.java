package com.pms.service;

import com.pms.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.resume.upload-dir}")
    private String uploadDir;

    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024; // 5MB

    public String storeResume(MultipartFile file, Long studentId) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Resume file is empty");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BadRequestException("Resume file must not exceed 5MB");
        }

        String originalName = file.getOriginalFilename() == null ? "resume.pdf" : file.getOriginalFilename();
        String extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')) : "";
        if (!extension.equalsIgnoreCase(".pdf") && !extension.equalsIgnoreCase(".doc") && !extension.equalsIgnoreCase(".docx")) {
            throw new BadRequestException("Only PDF, DOC or DOCX resumes are accepted");
        }

        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);

            String storedFileName = "student_" + studentId + "_" + UUID.randomUUID() + extension;
            Path target = dir.resolve(storedFileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + storedFileName;
        } catch (IOException ex) {
            throw new BadRequestException("Failed to store resume file: " + ex.getMessage());
        }
    }
}
