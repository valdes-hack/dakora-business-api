package com.techstore.techstore_api.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {

    @Autowired(required = false)
    private Cloudinary cloudinary;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${cloudinary.enabled:true}")
    private boolean cloudinaryEnabled;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Transforme le résultat de {@link #storeFile(MultipartFile)} en URL publique :
     * - URL Cloudinary (déjà complète) : renvoyée telle quelle ;
     * - nom de fichier local : préfixé par l'URL publique du backend (app.base-url).
     */
    public String resolvePublicUrl(String storedFile) {
        if (storedFile == null || storedFile.isBlank()) return null;
        if (storedFile.startsWith("http://") || storedFile.startsWith("https://")) return storedFile;
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return base + "/uploads/products/" + storedFile;
    }

    public String storeFile(MultipartFile file) {
        try {
            // Utiliser Cloudinary si activé et disponible ET configuré avec de vraies credentials
            if (cloudinaryEnabled && cloudinary != null) {
                // Vérification défensive : ne pas tenter si cloud-name est le placeholder
                try {
                    return storeFileOnCloudinary(file);
                } catch (Exception cloudEx) {
                    // Si Cloudinary échoue, fallback local
                    System.err.println("⚠️ Cloudinary indisponible, fallback local : " + cloudEx.getMessage());
                    return storeFileLocally(file);
                }
            }
            // Sinon, stockage local (fallback)
            return storeFileLocally(file);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de stocker le fichier", e);
        }
    }

    private String storeFileOnCloudinary(MultipartFile file) throws IOException {
        try {
            // Nom propre sans caractères spéciaux
            String originalName = file.getOriginalFilename() != null
                ? file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_")
                : "file";
            String publicId = UUID.randomUUID().toString() + "_" + originalName;

            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    // public_id SANS le préfixe "products/" — le folder s'en charge
                    "public_id", publicId,
                    "folder", "peoc_products",
                    "resource_type", "auto",
                    "overwrite", false
                )
            );

            String url = (String) uploadResult.get("secure_url");
            if (url == null || url.isBlank()) {
                throw new RuntimeException("Cloudinary n'a pas retourné d'URL sécurisée.");
            }
            return url;
        } catch (Exception e) {
            throw new RuntimeException("Erreur upload Cloudinary: " + e.getMessage(), e);
        }
    }

    private String storeFileLocally(MultipartFile file) throws IOException {
        // Sur Render, le filesystem est éphémère — on log un avertissement
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalName = file.getOriginalFilename() != null
            ? file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_")
            : "file";
        String fileName = UUID.randomUUID().toString() + "_" + originalName;
        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("📁 Fichier stocké localement : " + filePath);
        return fileName;
    }
}