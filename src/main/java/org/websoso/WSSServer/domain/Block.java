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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Block {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long blockId;

    @Column(nullable = false)
    private Long blockingId;

    @Column(nullable = false)
    private Long blockedId;

    @Builder
    private Block(Long blockingId, Long blockedId) {
        this.blockingId = blockingId;
        this.blockedId = blockedId;
    }

    public static Block create(Long blockingId, Long blockedId) {
        return new Block(blockingId, blockedId);
    }
}
