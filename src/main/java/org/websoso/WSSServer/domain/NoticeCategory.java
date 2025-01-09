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
public class NoticeCategory {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Byte noticeCategoryId;

    @Column(columnDefinition = "varchar(10)", nullable = false)
    private String noticeCategoryName;

    @Column(columnDefinition = "text", nullable = false)
    private String noticeCategoryImage;
}
