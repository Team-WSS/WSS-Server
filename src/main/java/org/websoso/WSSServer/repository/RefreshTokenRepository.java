package org.websoso.WSSServer.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.websoso.WSSServer.domain.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
