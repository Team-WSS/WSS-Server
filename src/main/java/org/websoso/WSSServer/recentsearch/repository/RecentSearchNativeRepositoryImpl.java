package org.websoso.WSSServer.recentsearch.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class RecentSearchNativeRepositoryImpl implements RecentSearchNativeRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void upsert(Long userId, String keyword, LocalDateTime searchedAt) {
        em.createNativeQuery("""
                        INSERT INTO recent_search (user_id, keyword, searched_at)
                        VALUES (:userId, :keyword, :searchedAt)
                        ON DUPLICATE KEY UPDATE searched_at = VALUES(searched_at)
                        """)
                .setParameter("userId", userId)
                .setParameter("keyword", keyword)
                .setParameter("searchedAt", searchedAt)
                .executeUpdate();
    }

    @Override
    @Transactional
    public void trimToMaxSize(Long userId, int maxSize) {
        em.createNativeQuery("""
                        DELETE FROM recent_search
                        WHERE user_id = :userId
                          AND id NOT IN (
                            SELECT id FROM (
                                SELECT id FROM recent_search
                                WHERE user_id = :userId
                                ORDER BY searched_at DESC
                                LIMIT :maxSize
                            ) keep
                          )
                        """)
                .setParameter("userId", userId)
                .setParameter("maxSize", maxSize)
                .executeUpdate();
    }

    @Override
    @Transactional
    public int deleteOlderThan(LocalDateTime threshold, int batchSize) {
        return em.createNativeQuery("""
                        DELETE FROM recent_search
                        WHERE searched_at < :threshold
                        LIMIT :batchSize
                        """)
                .setParameter("threshold", threshold)
                .setParameter("batchSize", batchSize)
                .executeUpdate();
    }
}
