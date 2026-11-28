package com.techstore.techstore_api.controller.admin;

import com.techstore.techstore_api.dto.request.CollectionRequest;
import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.dto.response.CollectionResponse;
import com.techstore.techstore_api.dto.response.ProductResponse;
import com.techstore.techstore_api.service.CollectionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONTRÔLEUR ADMIN DES COLLECTIONS.
 */
@RestController
@RequestMapping("/api/v1/admin/collections")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminCollectionController {

    private final CollectionService collectionService;

    @Data
    public static class BatchRequest {
        private String action; // activate, deactivate, publish, unpublish, delete
        private List<Long> ids;
    }

    /**
     * 1. LISTER TOUTES LES COLLECTIONS (ADMIN)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CollectionResponse>>> getAll() {
        List<CollectionResponse> collections = collectionService.getAllCollections();
        return ResponseEntity.ok(ApiResponse.<List<CollectionResponse>>builder()
                .status("success").code(200).data(collections).build());
    }

    /**
     * 2. DÉTAIL D'UNE COLLECTION
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CollectionResponse>> getById(@PathVariable Long id) {
        CollectionResponse response = collectionService.getCollectionById(id);
        return ResponseEntity.ok(ApiResponse.<CollectionResponse>builder()
                .status("success").code(200).data(response).build());
    }

    /**
     * 3. CRÉER UNE COLLECTION
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CollectionResponse>> create(@RequestBody CollectionRequest request) {
        CollectionResponse response = collectionService.createCollection(request);
        return ResponseEntity.status(201).body(ApiResponse.<CollectionResponse>builder()
                .status("success").code(201).message("Collection créée").data(response).build());
    }

    /**
     * 4. MODIFIER UNE COLLECTION
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CollectionResponse>> update(
            @PathVariable Long id,
            @RequestBody CollectionRequest request) {
        CollectionResponse response = collectionService.updateCollection(id, request);
        return ResponseEntity.ok(ApiResponse.<CollectionResponse>builder()
                .status("success").code(200).message("Collection mise à jour").data(response).build());
    }

    /**
     * 5. SUPPRIMER UNE COLLECTION
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        collectionService.deleteCollection(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success").code(200).message("Collection supprimée avec succès").build());
    }

    /**
     * 6. ACTIONS GROUPÉES (BATCH)
     */
    @PatchMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> batchAction(@RequestBody BatchRequest req) {
        if (req == null || req.getIds() == null || req.getIds().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                    .status("error").code(400).message("Aucune collection sélectionnée").build());
        }
        String action = req.getAction() != null ? req.getAction().toLowerCase() : "";
        switch (action) {
            case "activate" -> collectionService.batchActivate(req.getIds());
            case "deactivate" -> collectionService.batchDeactivate(req.getIds());
            case "publish" -> collectionService.batchPublish(req.getIds());
            case "unpublish" -> collectionService.batchUnpublish(req.getIds());
            case "delete" -> collectionService.batchDelete(req.getIds());
            default -> {
                return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                        .status("error").code(400).message("Action non supportée : " + req.getAction()).build());
            }
        }
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success").code(200).message("Action groupée '" + action + "' exécutée avec succès").build());
    }

    /**
     * 7. EXPORTER EN CSV
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(@RequestParam(required = false) List<Long> ids) {
        byte[] csv = collectionService.exportCsv(ids);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"collections.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    /**
     * 8. PRODUITS D'UNE COLLECTION (ADMIN)
     */
    @GetMapping("/{id}/products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = collectionService.getProductsForAdmin(id, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<ProductResponse>>builder()
                .status("success").code(200).data(products).build());
    }

    /**
     * 9. LIER UN PRODUIT À LA COLLECTION
     */
    @PostMapping("/{id}/products/{productId}")
    public ResponseEntity<ApiResponse<CollectionResponse>> addProduct(
            @PathVariable Long id,
            @PathVariable Long productId) {
        CollectionResponse response = collectionService.addProductToCollection(id, productId);
        return ResponseEntity.ok(ApiResponse.<CollectionResponse>builder()
                .status("success").code(200).message("Produit ajouté à la collection").data(response).build());
    }

    /**
     * 10. DÉLIER UN PRODUIT DE LA COLLECTION
     */
    @DeleteMapping("/{id}/products/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeProduct(
            @PathVariable Long id,
            @PathVariable Long productId) {
        collectionService.removeProductFromCollection(id, productId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success").code(200).message("Produit retiré de la collection").build());
    }

    /**
     * 11. REMPLACER TOUTE LA LISTE DES PRODUITS
     */
    @PutMapping("/{id}/products")
    public ResponseEntity<ApiResponse<CollectionResponse>> setProducts(
            @PathVariable Long id,
            @RequestBody List<Long> productIds) {
        CollectionResponse response = collectionService.setCollectionProducts(id, productIds);
        return ResponseEntity.ok(ApiResponse.<CollectionResponse>builder()
                .status("success").code(200).message("Produits de la collection mis à jour").data(response).build());
    }

    /**
     * 12. COLLECTIONS CONTENANT UN PRODUIT DONNÉ
     */
    @GetMapping("/by-product/{productId}")
    public ResponseEntity<ApiResponse<List<CollectionResponse>>> getByProduct(@PathVariable Long productId) {
        List<CollectionResponse> collections = collectionService.getCollectionsByProduct(productId);
        return ResponseEntity.ok(ApiResponse.<List<CollectionResponse>>builder()
                .status("success").code(200).data(collections).build());
    }
}
