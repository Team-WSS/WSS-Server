package org.websoso.WSSServer.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NovelKeywords {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long novelKeywordId;

    @Column(nullable = false)
    private Long novelId;

    @Column(nullable = false)
    private Integer keywordId;

    @Column
    private Long userId;

    private NovelKeywords(Long novelId, Integer keywordId, Long userId) {
        this.novelId = novelId;
        this.keywordId = keywordId;
        this.userId = userId;
    }

    public static NovelKeywords create(Long novelId, Integer keywordId, Long userId) {
        return new NovelKeywords(novelId, keywordId, userId);
    }
}
