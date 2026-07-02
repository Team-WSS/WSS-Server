package org.websoso.WSSServer.novel.repository;

import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.novel.domain.NovelStatistics;

@Repository
public interface NovelStatisticsRepository extends JpaRepository<NovelStatistics, Long> {

    @Modifying
    @Query(value = """
        INSERT IGNORE INTO novel_statistics (
            novel_id,
            average_rating,
            rating_sum,
            rating_count,
            popularity
        ) VALUES (
            :novelId,
            0.000,
            0.0,
            0,
            0
        )
        """, nativeQuery = true)
    int insertIfAbsent(@Param("novelId") Long novelId);

    @Modifying
    @Query(value = """
        UPDATE novel_statistics ns
        SET
            ns.average_rating = CASE
                WHEN ns.rating_count + :ratingCountDelta = 0 THEN 0
                ELSE CAST(ROUND(
                    (ns.rating_sum + :ratingSumDelta)
                    / (ns.rating_count + :ratingCountDelta),
                    3
                ) AS DECIMAL(4, 3))
            END,
            ns.rating_sum = ns.rating_sum + :ratingSumDelta,
            ns.rating_count = ns.rating_count + :ratingCountDelta,
            ns.popularity = ns.popularity + :popularityDelta
        WHERE ns.novel_id = :novelId
        """, nativeQuery = true)
    void updateByDelta(
            @Param("novelId") Long novelId,
            @Param("ratingSumDelta") BigDecimal ratingSumDelta,
            @Param("ratingCountDelta") Long ratingCountDelta,
            @Param("popularityDelta") Long popularityDelta
    );

    @Modifying
    @Query(value = """
        INSERT INTO novel_statistics (
            novel_id,
            average_rating,
            rating_sum,
            rating_count,
            popularity
        )
        SELECT
            n.novel_id,
            CAST(
                ROUND(COALESCE(AVG(NULLIF(un.user_novel_rating, 0)), 0), 3)
                AS DECIMAL(4, 3)
            ),
            CAST(
                ROUND(COALESCE(SUM(NULLIF(un.user_novel_rating, 0)), 0), 1)
                AS DECIMAL(19, 1)
            ),
            COUNT(NULLIF(un.user_novel_rating, 0)),
            COUNT(CASE
                WHEN un.is_interest = TRUE OR un.status <> 'QUIT' THEN 1
            END)
        FROM novel n
        LEFT JOIN user_novel un ON un.novel_id = n.novel_id
        GROUP BY n.novel_id
        ON DUPLICATE KEY UPDATE
            average_rating = VALUES(average_rating),
            rating_sum = VALUES(rating_sum),
            rating_count = VALUES(rating_count),
            popularity = VALUES(popularity)
        """, nativeQuery = true)
    int correctAll();
}
