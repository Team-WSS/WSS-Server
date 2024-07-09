package org.websoso.WSSServer.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Keyword {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Integer keywordId;

    @Column(columnDefinition = "varchar(10)", nullable = false)
    private String keywordName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_category_id", nullable = false)
    private KeywordCategory keywordCategory;

}
