package org.websoso.WSSServer.user.domain;

import static jakarta.persistence.GenerationType.IDENTITY;
import static org.websoso.WSSServer.user.exception.CustomBlockError.INVALID_AUTHORIZED_BLOCK;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.websoso.WSSServer.user.exception.CustomBlockException;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = Block.UNIQUE_CONSTRAINT_NAME,
                columnNames = {"blocked_id", "blocking_id"}
        ),
        indexes = @Index(
                name = "idx_block_blocking_id",
                columnList = "blocking_id"
        )
)
public class Block {

    public static final String UNIQUE_CONSTRAINT_NAME = "uk_block_blocking_blocked";

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long blockId;

    @Column(nullable = false)
    private Long blockingId;

    @Column(nullable = false)
    private Long blockedId;

    private Block(Long blockingId, Long blockedId) {
        this.blockingId = blockingId;
        this.blockedId = blockedId;
    }

    public static Block create(Long blockingId, Long blockedId) {
        return new Block(blockingId, blockedId);
    }

    public void validateOwner(Long userId) {
        if (!blockingId.equals(userId)) {
            throw new CustomBlockException(
                    INVALID_AUTHORIZED_BLOCK,
                    "only the user who created the block can delete it"
            );
        }
    }
}
