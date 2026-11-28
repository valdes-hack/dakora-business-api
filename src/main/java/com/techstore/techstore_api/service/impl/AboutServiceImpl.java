package com.techstore.techstore_api.service.impl;

import com.techstore.techstore_api.model.AboutContent;
import com.techstore.techstore_api.model.AppSetting;
import com.techstore.techstore_api.repository.AboutContentRepository;
import com.techstore.techstore_api.repository.CategoryRepository;
import com.techstore.techstore_api.repository.OrderRepository;
import com.techstore.techstore_api.repository.ProductRepository;
import com.techstore.techstore_api.repository.UserRepository;
import com.techstore.techstore_api.service.AboutService;
import com.techstore.techstore_api.service.AppSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AboutServiceImpl implements AboutService {

    private final AboutContentRepository aboutContentRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final AppSettingService appSettingService;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPublicAboutData() {
        AboutContent content = getOrCreate("LIVE");

        Map<String, Object> result = new HashMap<>();
        result.put("content", content);

        // Statistiques dynamiques réelles calculées depuis la BDD
        long totalProducts = productRepository.count();
        long totalCategories = categoryRepository.count();
        long totalClients = userRepository.count();
        long totalOrders = orderRepository.count();

        Map<String, Object> dynamicStats = new HashMap<>();
        dynamicStats.put("totalProducts", totalProducts);
        dynamicStats.put("totalCategories", totalCategories);
        dynamicStats.put("totalClients", totalClients);
        dynamicStats.put("totalOrders", totalOrders);
        dynamicStats.put("yearsExperience", 4); // Depuis 2020/2021
        result.put("dynamicStats", dynamicStats);

        // Fallback coordonnées globales
        try {
            AppSetting appSetting = appSettingService.getSettings();
            result.put("globalContact", Map.of(
                    "siteName", appSetting.getSiteName(),
                    "email", appSetting.getContactEmail(),
                    "phone", appSetting.getContactPhone(),
                    "address", appSetting.getContactAddress()
            ));
        } catch (Exception ignored) {}

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public AboutContent getAdminAboutContent() {
        // En priorité le DRAFT s'il existe, sinon le LIVE
        return aboutContentRepository.findByVersion("DRAFT")
                .orElseGet(() -> getOrCreate("LIVE"));
    }

    @Override
    @Transactional
    public AboutContent saveAdminAboutContent(AboutContent incoming, boolean publishDirectly) {
        String targetVersion = publishDirectly ? "LIVE" : "DRAFT";
        AboutContent existing = aboutContentRepository.findByVersion(targetVersion)
                .orElse(AboutContent.builder().version(targetVersion).build());

        copyFields(incoming, existing);
        existing.setVersion(targetVersion);
        existing.setIsPublished(publishDirectly || existing.getIsPublished());

        AboutContent saved = aboutContentRepository.save(existing);

        if (publishDirectly) {
            // Synchroniser le draft avec le live
            aboutContentRepository.findByVersion("DRAFT").ifPresent(draft -> {
                copyFields(saved, draft);
                draft.setVersion("DRAFT");
                aboutContentRepository.save(draft);
            });
        }

        log.info("💾 [CMS ABOUT] Sauvegarde effectuée version {} (publishDirectly={})", targetVersion, publishDirectly);
        return saved;
    }

    @Override
    @Transactional
    public AboutContent publishDraft() {
        AboutContent draft = aboutContentRepository.findByVersion("DRAFT")
                .orElseGet(() -> getOrCreate("LIVE"));

        AboutContent live = aboutContentRepository.findByVersion("LIVE")
                .orElse(AboutContent.builder().version("LIVE").build());

        copyFields(draft, live);
        live.setVersion("LIVE");
        live.setIsPublished(true);

        AboutContent saved = aboutContentRepository.save(live);
        log.info("🚀 [CMS ABOUT] Draft publié avec succès vers LIVE");
        return saved;
    }

    private AboutContent getOrCreate(String version) {
        return aboutContentRepository.findByVersion(version).orElseGet(() -> {
            AboutContent initial = AboutContent.builder()
                    .version(version)
                    .companyName("DAKORA BUSINESS")
                    .mainTitle("Leader en technologies et solutions pour l'élevage")
                    .subtitle("Votre partenaire de confiance pour des équipements modernes, des couveuses haute performance et un accompagnement de pointe.")
                    .generalDescription("DAKORA BUSINESS (PEOC) est une entreprise spécialisée dans la distribution d'équipements agricoles et avicoles modernes au Cameroun et en Afrique Centrale. Nous innovons au quotidien pour offrir aux éleveurs des solutions technologiques robustes et rentables.")
                    .historyTitle("Notre Histoire & Vision")
                    .historyText("Fondée avec l'ambition de transformer l'aviculture et l'élevage en Afrique, DAKORA BUSINESS a débuté en fournissant des couveuses automatisées fiables aux éleveurs de l'Ouest Cameroun. Aujourd'hui, notre catalogue s'étend à des centaines d'équipements de pointe.")
                    .historyYear("2020")
                    .missionText("Démocratiser l'accès aux technologies d'élevage modernes afin d'accroître la productivité, la sécurité sanitaire et la rentabilité de chaque éleveur.")
                    .visionText("Devenir la référence incontournable du matériel d'élevage intelligent en Afrique d'ici 2030.")
                    .missionVisible(true)
                    .visionVisible(true)
                    .valuesJson("[{\"title\":\"Innovation Continue\",\"description\":\"Sélection rigoureuse des technologies les plus performantes\",\"icon\":\"Sparkles\"},{\"title\":\"Excellence & Fiabilité\",\"description\":\"Matériel testé et garanti pour résister aux contraintes du terrain\",\"icon\":\"ShieldCheck\"},{\"title\":\"Proximité & Conseil\",\"description\":\"Assistance technique réactive 24/7 pour nos éleveurs\",\"icon\":\"HeartHandshake\"}]")
                    .teamJson("[{\"id\":1,\"name\":\"Valdes Tenwo\",\"role\":\"Directeur Général & Fondateur\",\"bio\":\"Passionné d'innovation agrotechnologique et d'ingénierie.\",\"isVisible\":true}]")
                    .commitmentsJson("[{\"title\":\"Garantie & SAV Réactif\",\"description\":\"Service après-vente dédié et pièces de rechange disponibles en stock\",\"icon\":\"Wrench\"},{\"title\":\"Livraison Rapide\",\"description\":\"Expédition sécurisée partout au Cameroun et en sous-région CEMAC\",\"icon\":\"Truck\"},{\"title\":\"Formation & Support\",\"description\":\"Guides d'utilisation et conseils personnalisés pour chaque équipement\",\"icon\":\"BookOpen\"}]")
                    .isPublished(true)
                    .build();
            return aboutContentRepository.save(initial);
        });
    }

    private void copyFields(AboutContent src, AboutContent dest) {
        if (src.getCompanyName() != null) dest.setCompanyName(src.getCompanyName());
        if (src.getMainTitle() != null) dest.setMainTitle(src.getMainTitle());
        if (src.getSubtitle() != null) dest.setSubtitle(src.getSubtitle());
        if (src.getGeneralDescription() != null) dest.setGeneralDescription(src.getGeneralDescription());
        if (src.getLogoUrl() != null) dest.setLogoUrl(src.getLogoUrl());
        if (src.getMainImageUrl() != null) dest.setMainImageUrl(src.getMainImageUrl());
        if (src.getCtaText() != null) dest.setCtaText(src.getCtaText());
        if (src.getCtaUrl() != null) dest.setCtaUrl(src.getCtaUrl());
        if (src.getHistoryTitle() != null) dest.setHistoryTitle(src.getHistoryTitle());
        if (src.getHistoryText() != null) dest.setHistoryText(src.getHistoryText());
        if (src.getHistoryYear() != null) dest.setHistoryYear(src.getHistoryYear());
        if (src.getHistoryImagesJson() != null) dest.setHistoryImagesJson(src.getHistoryImagesJson());
        if (src.getMissionText() != null) dest.setMissionText(src.getMissionText());
        if (src.getVisionText() != null) dest.setVisionText(src.getVisionText());
        if (src.getMissionVisible() != null) dest.setMissionVisible(src.getMissionVisible());
        if (src.getVisionVisible() != null) dest.setVisionVisible(src.getVisionVisible());
        if (src.getValuesJson() != null) dest.setValuesJson(src.getValuesJson());
        if (src.getStatsJson() != null) dest.setStatsJson(src.getStatsJson());
        if (src.getTeamJson() != null) dest.setTeamJson(src.getTeamJson());
        if (src.getCommitmentsJson() != null) dest.setCommitmentsJson(src.getCommitmentsJson());
        if (src.getGalleryJson() != null) dest.setGalleryJson(src.getGalleryJson());
        if (src.getSocialLinksJson() != null) dest.setSocialLinksJson(src.getSocialLinksJson());
        if (src.getSeoJson() != null) dest.setSeoJson(src.getSeoJson());
        if (src.getIsPublished() != null) dest.setIsPublished(src.getIsPublished());
    }
}
