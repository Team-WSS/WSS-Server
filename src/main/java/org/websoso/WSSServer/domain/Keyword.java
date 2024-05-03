package org.websoso.WSSServer.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.websoso.WSSServer.domain.common.KeywordCategory;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Keyword {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long keywordId;

    @Column(nullable = false)
    private KeywordCategory categoryName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KeywordCategory keywordName;
}
