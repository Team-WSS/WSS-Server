package org.websoso.WSSServer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.dto.User.NicknameValidation;
import org.websoso.WSSServer.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public NicknameValidation isNicknameAvailable(String nickname) {
        boolean isNicknameTaken = userRepository.existsByNickname(nickname);
        return NicknameValidation.of(isNicknameTaken);
    }
}
