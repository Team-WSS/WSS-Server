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
public class NovelStatistics {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long novelStatisticsId;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer novelFeedCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer watchingCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer interestCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer watchedCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer quitCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer universeCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer vibeCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer materialCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer charactersCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer relationshipCount;

    @OneToOne
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

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
