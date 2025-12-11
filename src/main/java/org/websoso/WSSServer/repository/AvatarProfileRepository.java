package org.websoso.WSSServer.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.AvatarProfile;

@Repository
public interface AvatarProfileRepository extends JpaRepository<AvatarProfile, Long> {
    List<AvatarProfile> findAllByAvatarProfileIdNot(Long avatarProfileId);

}
