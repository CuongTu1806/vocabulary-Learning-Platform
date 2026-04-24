package com.example.learningVocabularyPlatform.service.impl;

import com.example.learningVocabularyPlatform.config.StorageProperties;
import com.example.learningVocabularyPlatform.exception.ResourceNotFoundException;
import com.example.learningVocabularyPlatform.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalFileStorageService implements FileStorageService {

    private final StorageProperties storageProperties;

    private Path rootPath() {
        return Paths.get(storageProperties.getRoot()).toAbsolutePath().normalize();
    }

    @Override
    public String store(MultipartFile file, String subdirectory) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File rỗng");
        }
        long max = storageProperties.getMaxFileSizeBytes();
        if (file.getSize() > max) {
            throw new IllegalArgumentException("File vượt quá " + (max / (1024 * 1024)) + "MB");
        }
        String safeSub = subdirectory.replace("..", "").replace('\\', '/').trim();
        if (safeSub.isEmpty()) {
            throw new IllegalArgumentException("Thư mục không hợp lệ");
        }
        String original = file.getOriginalFilename();
        String suffix = sanitizeForFilename(original);

        Path dir = rootPath().resolve(safeSub).normalize();
        if (!dir.startsWith(rootPath())) {
            throw new IllegalArgumentException("Đường dẫn không hợp lệ");
        }
        Files.createDirectories(dir);

        String storedName = UUID.randomUUID() + (suffix.isEmpty() ? "" : "_" + suffix);
        Path target = dir.resolve(storedName).normalize();
        if (!target.startsWith(rootPath())) {
            throw new IllegalArgumentException("Đường dẫn không hợp lệ");
        }
        Files.copy(file.getInputStream(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        return rootPath().relativize(target).toString().replace('\\', '/');
    }

    private static String sanitizeForFilename(String original) {
        if (original == null || original.isBlank()) {
            return "";
        }
        String name = original.trim();
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0 && slash < name.length() - 1) {
            name = name.substring(slash + 1);
        }
        String clean = name.replaceAll("[^a-zA-Z0-9._\\-\\u00C0-\\u024F]", "_");
        return clean.length() > 120 ? clean.substring(0, 120) : clean;
    }

    @Override
    public void deleteIfExists(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        Path p = rootPath().resolve(relativePath).normalize();
        if (!p.startsWith(rootPath())) {
            return;
        }
        try {
            Files.deleteIfExists(p);
        } catch (IOException ignored) {
            // log in production
        }
    }

    @Override
    public Resource loadAsResource(String relativePath) {
        Path p = rootPath().resolve(relativePath).normalize();
        if (!p.startsWith(rootPath()) || !Files.isRegularFile(p)) {
            throw new ResourceNotFoundException("File không tồn tại");
        }
        return new PathResource(p);
    }

    @Override
    public boolean exists(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return false;
        }
        Path p = rootPath().resolve(relativePath).normalize();
        return p.startsWith(rootPath()) && Files.isRegularFile(p);
    }
}
