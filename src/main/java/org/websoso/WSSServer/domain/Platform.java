package org.websoso.WSSServer.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class Platform {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long platformId;

    @Column(columnDefinition = "varchar(10)", nullable = false)
    private String platformName;

    @Column(nullable = false)
    private String platformUrl;

    @ManyToOne
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;
}
