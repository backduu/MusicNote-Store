package com.store.store.repository;

import com.store.store.domain.entity.Product;
import com.store.store.domain.enums.Difficulty;
import com.store.store.domain.enums.MetricType;
import com.store.store.domain.enums.ProductStatus;
import com.store.store.domain.enums.ProductType;
import com.store.store.dto.ProductDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    @Query("SELECT p FROM Product p " +
            "WHERE p.createdAt BETWEEN :start AND :end " +
            "AND p.status = :status " +
            "ORDER BY p.createdAt DESC ")
    List<Product> findNewProducts(
            LocalDateTime start,
            LocalDateTime end,
            ProductStatus status
    );

    @Query("""
            SELECT p
            FROM Product p
            JOIN ProductTag pt ON pt.product = p
            WHERE p.status = :status
              AND pt.metricType = :metricType
              AND p.createdAt BETWEEN :start AND :end
            ORDER BY pt.value DESC
    """)
    List<Product> findTopProductsByMetricAndPeriod(
            @Param("status") ProductStatus status,
            @Param("metricType") MetricType metricType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT p.genre
        FROM Product p
        WHERE p.genre IS NOT NULL
    """)
    List<String> findDistinctGenres();

    @Query("""
        SELECT p
        FROM Product p
        JOIN ProductTag pt ON pt.product = p
        WHERE p.status = :status
          AND (:type = 'ALL' OR p.type = :type)
          AND (:region IS NULL OR p.country = :region)
          AND (:genre IS NULL OR p.genre = :genre)
          AND p.createdAt BETWEEN :start AND :end
          AND pt.metricType = :metricType
          AND (:searchTerm IS NULL
                OR
                    LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                OR
                    LOWER(p.creator) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
              )
        ORDER BY pt.value DESC
    """)
    List<Product> findSongMarketProductsByMetric(
            @Param("status") ProductStatus status,
            @Param("type") ProductType type,
            @Param("region") String region,
            @Param("genre") String genre,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("metricType") MetricType metricType,
            Pageable pageable,
            @Param("searchTerm") String searchTerm
    );

    @Query("""
        SELECT p
        FROM Product p
        WHERE p.status = :status
          AND (p.type = :type)
          AND (:genre IS NULL OR p.genre = :genre)
          AND p.createdAt BETWEEN :start AND :end
          AND (
                (:region = 'Korea' AND p.country = 'Korea')
              OR
                (:region = 'FOREIGN' OR p.country <> 'Korea')
          )
          AND (:searchTerm IS NULL
              OR
                  LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
              OR
                  LOWER(p.creator) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
              )
        ORDER BY p.createdAt DESC
    """)
    List<Product> findSongMarketProductsByReleased(
            @Param("status") ProductStatus status,
            @Param("type") ProductType type,
            @Param("region") String region,
            @Param("genre") String genre,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable,
            @Param("searchTerm") String searchTerm
    );

    @Query("""
    SELECT p
    FROM Product p
    JOIN ProductTag pt ON pt.product = p
    JOIN Sheet s ON s.product = p              
    WHERE p.status = :status
      AND p.type = :type
      AND (:region = 'Korea' AND p.country = 'Korea'
           OR :region = 'FOREIGN' AND p.country <> 'Korea')
      AND (:genre IS NULL OR p.genre = :genre)
      AND (:instrument IS NULL OR s.instrument = :instrument) 
      AND (:difficulty IS NULL OR s.difficulty = :difficulty) 
      AND (:era IS NULL OR s.era = :era)                     
      AND p.createdAt BETWEEN :start AND :end
      AND pt.metricType = :metricType
      AND (:searchTerm IS NULL
           OR LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
           OR LOWER(p.creator) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
    ORDER BY pt.value DESC
""")
    List<Product> findSheetArchiveByMetric(
            @Param("status") ProductStatus status,
            @Param("type") ProductType type,
            @Param("region") String region,
            @Param("genre") String genre,
            @Param("instrument") String instrument,
            @Param("difficulty") Difficulty difficulty,
            @Param("era") String era,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("metricType") MetricType metricType,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    @Query("""
    SELECT p
    FROM Product p
    JOIN Sheet s ON s.product = p
    WHERE p.status = :status
      AND p.type = :type
      AND (:region = 'Korea' AND p.country = 'Korea'
           OR :region = 'FOREIGN' AND p.country <> 'Korea')
      AND (:genre IS NULL OR p.genre = :genre)
      AND (:instrument IS NULL OR s.instrument = :instrument)
      AND (:difficulty IS NULL OR s.difficulty = :difficulty)
      AND (:era IS NULL OR s.era = :era)
      AND p.createdAt BETWEEN :start AND :end
      AND (:searchTerm IS NULL
           OR LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
           OR LOWER(p.creator) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
    ORDER BY p.createdAt DESC
""")
    List<Product> findSheetArchiveByReleased(
            @Param("status") ProductStatus status,
            @Param("type") ProductType type,
            @Param("region") String region,
            @Param("genre") String genre,
            @Param("instrument") String instrument,
            @Param("difficulty") Difficulty difficulty,
            @Param("era") String era,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );
}
