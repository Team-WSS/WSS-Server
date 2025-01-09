package org.websoso.WSSServer.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.websoso.WSSServer.domain.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long noticeId;

    @Column(columnDefinition = "varchar(200)", nullable = false)
    private String noticeTitle;

    @Column(columnDefinition = "varchar(2000)", nullable = false)
    private String noticeContent;

    @Column(columnDefinition = "text", nullable = false)
    private String noticeIcon;

    @Column
    private Long userId;

    @Builder
    private Notice(String noticeTitle, String noticeContent, Long userId) {
        this.noticeTitle = noticeTitle;
        this.noticeContent = noticeContent;
        this.userId = userId;
    }

    public void updateNotice(String noticeTitle, String noticeContent, Long userId) {
        this.noticeTitle = noticeTitle;
        this.noticeContent = noticeContent;
        this.userId = userId;
    }

}
