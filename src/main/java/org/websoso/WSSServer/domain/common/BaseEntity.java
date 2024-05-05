package org.websoso.WSSServer.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(nullable = false)
    protected String createdDate;

    @LastModifiedDate
    @Column(nullable = false)
    protected String modifiedDate;

    @PrePersist
    public void onPrePersist() {
        String formattedDateTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd a hh:mm").withLocale(Locale.forLanguageTag("ko")));

        this.createdDate = formattedDateTime;
        this.modifiedDate = formattedDateTime;
    }

    @PreUpdate
    public void onPreUpdate() {
        this.modifiedDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd a hh:mm").withLocale(Locale.forLanguageTag("ko")));
    }
}