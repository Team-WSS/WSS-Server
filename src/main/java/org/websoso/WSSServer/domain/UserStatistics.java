package org.websoso.WSSServer.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.lang.reflect.Field;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStatistics {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long userStatisticsId;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer watchingNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer interestNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer watchedNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer quitNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer roNovelNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer rfNovelNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer blNovelNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer faNovelNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer mfNovelNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer wuNovelNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer lnNovelNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer drNovelNovelCount;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer myNovelNovelCount;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void increaseField(String fieldName) {
        try {
            Field field = this.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Integer currentValue = (Integer) field.get(this);
            field.set(this, currentValue + 1);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Field not found or not accessible", e);
        }
    }

}
