package org.websoso.WSSServer.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final UserRepository userRepository;

    public boolean validate(Long resourceId, Long userId, Class<?> resourceType) {

    }
}
