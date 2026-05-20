package org.websoso.WSSServer.recentsearch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(
        name = "recent_search",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_keyword", columnNames = {"user_id", "keyword"}),
        indexes = {
                @Index(name = "idx_user_searched_at", columnList = "user_id, searched_at"),
                @Index(name = "idx_searched_at", columnList = "searched_at")
        }

)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecentSearch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("소설 최근 검색어 PK")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Comment("사용자 ID")
    private Long userId;

    @Column(name = "keyword", nullable = false, length = 100)
    @Comment("검색어")
    private String keyword;

    @Column(nullable = false)
    @Comment("검색 날짜")
    private LocalDateTime searchedAt;

    public RecentSearch(Long userId, String keyword, LocalDateTime searchedAt) {
        this.userId = userId;
        this.keyword = keyword;
        this.searchedAt = searchedAt;
    }

}
