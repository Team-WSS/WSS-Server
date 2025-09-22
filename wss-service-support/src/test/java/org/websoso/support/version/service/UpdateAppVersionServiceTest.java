package org.websoso.support.version.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.support.version.domain.MinimumVersion;
import org.websoso.support.version.domain.OS;
import org.websoso.support.version.dto.MinimumVersionUpdateRequest;
import org.websoso.support.version.repository.MinimumVersionRepository;

@ExtendWith(MockitoExtension.class)
class UpdateAppVersionServiceTest {

    @InjectMocks
    private UpdateAppVersionService updateAppVersionService;

    @Mock
    private MinimumVersionRepository minimumVersionRepository;

    @DisplayName("새로운 OS의 최소 버전을 등록할 수 있다.")
    @Test
    void createNewVersionIfOsDoesNotExist() {
        // given
        OS os = OS.ANDROID;
        String newVersion = "1.0.1";
        MinimumVersionUpdateRequest request = new MinimumVersionUpdateRequest(os.getLabel(), newVersion);

        given(minimumVersionRepository.findById(os)).willReturn(Optional.empty());

        // when
        updateAppVersionService.updateMinimumVersion(request);

        // then
        verify(minimumVersionRepository, times(1)).save(any(MinimumVersion.class));
    }

    @DisplayName("기존 OS의 최소 버전을 업데이트할 수 있다.")
    @Test
    void updateExistingVersionIfOsExists() {
        // given
        OS os = OS.IOS;
        String initialVersion = "1.0.0";
        String updatedVersion = "1.0.2";
        MinimumVersionUpdateRequest request = new MinimumVersionUpdateRequest(os.getLabel(), updatedVersion);
        MinimumVersion minimumVersion = MinimumVersion.create(os, initialVersion);

        given(minimumVersionRepository.findById(os)).willReturn(Optional.of(minimumVersion));

        // when
        updateAppVersionService.updateMinimumVersion(request);

        // then
        verify(minimumVersionRepository, times(1)).save(any(MinimumVersion.class));
    }
}
