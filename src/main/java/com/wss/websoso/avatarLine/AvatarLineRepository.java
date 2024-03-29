package com.wss.websoso.avatarLine;

import com.wss.websoso.avatar.Avatar;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AvatarLineRepository extends JpaRepository<AvatarLine, Long> {

    @Query(value = "SELECT al FROM AvatarLine al WHERE al.avatar.avatarId = ?1")
    List<AvatarLine> findByAvatarId(Long avatarId);

    List<AvatarLine> findByAvatar(Avatar avatar);
}
