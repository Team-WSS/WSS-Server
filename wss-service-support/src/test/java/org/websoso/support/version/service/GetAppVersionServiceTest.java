package org.websoso.support.version.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.support.version.domain.MinimumVersion;
import org.websoso.support.version.domain.OS;
import org.websoso.support.version.dto.MinimumVersionGetResponse;
import org.websoso.support.version.exception.CustomMinimumVersionException;
import org.websoso.support.version.repository.MinimumVersionRepository;

@ExtendWith(MockitoExtension.class)
class GetAppVersionServiceTest {

    @InjectMocks
    private GetAppVersionService getAppVersionService;

    @Mock
    private MinimumVersionRepository minimumVersionRepository;

    @DisplayName("최소 버전을 조회할 수 있다.")
    @Test
    void returnsMinimumVersionIfOsExists() {
        // given
        OS os = OS.ANDROID;
        String minimumVersion = "1.0.0";
        MinimumVersion minimumVersionEntity = MinimumVersion.create(os, minimumVersion);

        given(minimumVersionRepository.findById(os)).willReturn(Optional.of(minimumVersionEntity));

        // when
        MinimumVersionGetResponse response = getAppVersionService.getMinimumVersion(os.getLabel());

        // then
        assertThat(response.minimumVersion()).isEqualTo(minimumVersion);
    }

    @DisplayName("최소 버전이 존재하지 않으면 예외가 발생한다.")
    @Test
    void throwsExceptionIfOsDoesNotExist() {
        // given
        OS os = OS.ANDROID;

        given(minimumVersionRepository.findById(os)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getAppVersionService.getMinimumVersion(os.getLabel()))
                .isInstanceOf(CustomMinimumVersionException.class);
    }
}
