package com.techstore.techstore_api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "about_contents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AboutContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false)
    @Builder.Default
    private String version = "LIVE"; // "LIVE" (published) or "DRAFT"

    // 1. Présentation générale
    @Column(name = "company_name", length = 255)
    @Builder.Default
    private String companyName = "DAKORA BUSINESS";

    @Column(name = "main_title", length = 500)
    @Builder.Default
    private String mainTitle = "Leader en technologies et solutions pour l'élevage en Afrique Centrale";

    @Column(name = "subtitle", length = 1000)
    @Builder.Default
    private String subtitle = "Votre partenaire de confiance pour des équipements modernes, des couveuses haute performance et un accompagnement de pointe.";

    @Column(name = "general_description", columnDefinition = "LONGTEXT")
    private String generalDescription;

    @Column(name = "logo_url", length = 1000)
    private String logoUrl;

    @Column(name = "main_image_url", length = 1000)
    private String mainImageUrl;

    @Column(name = "cta_text", length = 100)
    @Builder.Default
    private String ctaText = "Explorer nos collections";

    @Column(name = "cta_url", length = 500)
    @Builder.Default
    private String ctaUrl = "/collections";

    // 2. Notre histoire
    @Column(name = "history_title", length = 255)
    @Builder.Default
    private String historyTitle = "Notre Histoire";

    @Column(name = "history_text", columnDefinition = "LONGTEXT")
    private String historyText;

    @Column(name = "history_year", length = 50)
    @Builder.Default
    private String historyYear = "2020";

    @Column(name = "history_images_json", columnDefinition = "LONGTEXT")
    private String historyImagesJson;

    // 3. Notre mission et notre vision
    @Column(name = "mission_text", columnDefinition = "LONGTEXT")
    private String missionText;

    @Column(name = "vision_text", columnDefinition = "LONGTEXT")
    private String visionText;

    @Column(name = "mission_visible")
    @Builder.Default
    private Boolean missionVisible = true;

    @Column(name = "vision_visible")
    @Builder.Default
    private Boolean visionVisible = true;

    // 4. Nos valeurs (JSON array)
    @Column(name = "values_json", columnDefinition = "LONGTEXT")
    private String valuesJson;

    // 5. Nos chiffres clés (JSON array)
    @Column(name = "stats_json", columnDefinition = "LONGTEXT")
    private String statsJson;

    // 6. Notre équipe (JSON array)
    @Column(name = "team_json", columnDefinition = "LONGTEXT")
    private String teamJson;

    // 7. Nos engagements et avantages (JSON array)
    @Column(name = "commitments_json", columnDefinition = "LONGTEXT")
    private String commitmentsJson;

    // 8. Galerie et médias (JSON array)
    @Column(name = "gallery_json", columnDefinition = "LONGTEXT")
    private String galleryJson;

    // 9. Réseaux sociaux et coordonnées (JSON object)
    @Column(name = "social_links_json", columnDefinition = "LONGTEXT")
    private String socialLinksJson;

    // 10. SEO de la page (JSON object)
    @Column(name = "seo_json", columnDefinition = "LONGTEXT")
    private String seoJson;

    @Column(name = "is_published")
    @Builder.Default
    private Boolean isPublished = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
