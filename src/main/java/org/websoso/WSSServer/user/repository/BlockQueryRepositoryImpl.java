package org.websoso.WSSServer.user.repository;

import static org.websoso.WSSServer.user.domain.QAvatarProfile.avatarProfile;
import static org.websoso.WSSServer.user.domain.QBlock.block;
import static org.websoso.WSSServer.user.domain.QUser.user;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.user.repository.projection.BlockInfoRow;

@Repository
@RequiredArgsConstructor
public class BlockQueryRepositoryImpl implements BlockQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<BlockInfoRow> findBlockInfoRows(Long blockingId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        BlockInfoRow.class,
                        block.blockId,
                        user.userId,
                        user.nickname,
                        avatarProfile.avatarProfileImage
                ))
                .from(block)
                .join(user).on(block.blockedId.eq(user.userId))
                .leftJoin(avatarProfile).on(user.avatarProfileId.eq(avatarProfile.avatarProfileId))
                .where(block.blockingId.eq(blockingId))
                .orderBy(block.blockId.desc())
                .fetch();
    }
}
