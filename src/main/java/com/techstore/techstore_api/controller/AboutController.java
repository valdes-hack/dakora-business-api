package com.techstore.techstore_api.controller;

import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.model.AboutContent;
import com.techstore.techstore_api.service.AboutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AboutController {

    private final AboutService aboutService;

    /**
     * Lecture publique du contenu "À Propos" + stats réelles
     */
    @GetMapping("/about")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPublicAbout() {
        Map<String, Object> data = aboutService.getPublicAboutData();
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status("success")
                .code(200)
                .data(data)
                .build());
    }

    /**
     * Lecture admin du contenu "À Propos" (draft ou live)
     */
    @GetMapping("/admin/about")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AboutContent>> getAdminAbout() {
        AboutContent data = aboutService.getAdminAboutContent();
        return ResponseEntity.ok(ApiResponse.<AboutContent>builder()
                .status("success")
                .code(200)
                .data(data)
                .build());
    }

    /**
     * Sauvegarde admin (mode draft ou publication directe)
     */
    @PutMapping("/admin/about")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AboutContent>> saveAdminAbout(
            @RequestBody AboutContent content,
            @RequestParam(defaultValue = "false") boolean publish) {
        AboutContent saved = aboutService.saveAdminAboutContent(content, publish);
        return ResponseEntity.ok(ApiResponse.<AboutContent>builder()
                .status("success")
                .code(200)
                .message(publish ? "Contenu 'À propos' publié avec succès" : "Brouillon 'À propos' enregistré")
                .data(saved)
                .build());
    }

    /**
     * Publication explicite du brouillon
     */
    @PostMapping("/admin/about/publish")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AboutContent>> publishAbout() {
        AboutContent published = aboutService.publishDraft();
        return ResponseEntity.ok(ApiResponse.<AboutContent>builder()
                .status("success")
                .code(200)
                .message("Contenu 'À propos' publié avec succès")
                .data(published)
                .build());
    }
}
