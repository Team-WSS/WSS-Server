package org.websoso.WSSServer.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.user.domain.Avatar;

@Repository
public interface AvatarRepository extends JpaRepository<Avatar, Byte> {
}
