package com.techstore.techstore_api.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name:your-cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key:your-api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret:your-api-secret}")
    private String apiSecret;

    /**
     * Bean créé uniquement si cloudinary.enabled=true ET les credentials sont configurés.
     * Si absent, FileStorageService (@Autowired required=false) reçoit null
     * et bascule automatiquement sur le stockage local.
     */
    @Bean
    @ConditionalOnProperty(name = "cloudinary.enabled", havingValue = "true", matchIfMissing = false)
    public Cloudinary cloudinary() {
        if ("your-cloud-name".equals(cloudName) || cloudName == null || cloudName.isBlank()
                || "your-api-key".equals(apiKey) || apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "cloudinary.enabled=true mais les credentials Cloudinary ne sont pas configurés. " +
                "Définissez CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY et CLOUDINARY_API_SECRET, " +
                "ou passez cloudinary.enabled=false pour utiliser le stockage local.");
        }
        System.out.println("✅ Cloudinary configuré : cloud=" + cloudName);
        return new Cloudinary("cloudinary://" + apiKey + ":" + apiSecret + "@" + cloudName);
    }
}
