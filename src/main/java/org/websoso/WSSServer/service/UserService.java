package org.websoso.WSSServer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.dto.User.NicknameValidation;
import org.websoso.WSSServer.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public NicknameValidation isNicknameAvailable(String nickname) {
        boolean isNicknameTaken = userRepository.existsByNickname(nickname);
        return NicknameValidation.of(isNicknameTaken);
    }
}
