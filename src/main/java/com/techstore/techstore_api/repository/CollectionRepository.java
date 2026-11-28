package com.techstore.techstore_api.repository;

import com.techstore.techstore_api.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

    @Modifying
    @Query("UPDATE Collection c SET c.isActive = :active WHERE c.id IN :ids")
    void updateIsActiveByIds(@Param("active") boolean active, @Param("ids") List<Long> ids);

    @Modifying
    @Query("UPDATE Collection c SET c.isPublished = :pub WHERE c.id IN :ids")
    void updateIsPublishedByIds(@Param("pub") boolean pub, @Param("ids") List<Long> ids);

    @Modifying
    @Query("DELETE FROM Collection c WHERE c.id IN :ids")
    void deleteAllByIdInBatch(@Param("ids") List<Long> ids);

    Optional<Collection> findBySlug(String slug);

    Optional<Collection> findByNameIgnoreCase(String name);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    // Public : uniquement les collections actives et publiées, triées
    List<Collection> findByIsActiveTrueAndIsPublishedTrueOrderBySortOrderAscIdAsc();

    // Fallback pour compatibilité
    List<Collection> findByIsActiveTrueOrderBySortOrderAscIdAsc();

    // Admin : toutes les collections
    List<Collection> findAllByOrderBySortOrderAscIdAsc();

    long countByIsActiveTrue();

    long countByIsPublishedTrue();

    /**
     * Nombre de produits ACTIFS liés à une collection (pour afficher le compteur sur la carte).
     */
    @Query("""
            SELECT COUNT(p) FROM Product p
            JOIN ProductCollection pc ON pc.product.id = p.id
            WHERE pc.collection.id = :collectionId AND p.isActive = true
            """)
    long countActiveProducts(@Param("collectionId") Long collectionId);
}
