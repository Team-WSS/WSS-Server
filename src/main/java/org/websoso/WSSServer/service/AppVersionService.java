package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomMinimumVersionError.MINIMUM_VERSION_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.MinimumVersion;
import org.websoso.WSSServer.domain.common.OS;
import org.websoso.WSSServer.dto.AppVersion.MinimumVersionGetResponse;
import org.websoso.WSSServer.dto.AppVersion.MinimumVersionUpdateRequest;
import org.websoso.WSSServer.exception.exception.CustomMinimumVersionException;
import org.websoso.WSSServer.repository.MinimumVersionRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AppVersionService {

    private final MinimumVersionRepository minimumVersionRepository;

    @Transactional(readOnly = true)
    public MinimumVersionGetResponse getMinimumVersion(String os) {
        return MinimumVersionGetResponse.of(minimumVersionRepository.findById(OS.fromLabel(os))
                .orElseThrow(() -> new CustomMinimumVersionException(MINIMUM_VERSION_NOT_FOUND,
                        "the minimum supported version for the specified OS could not be found")));
    }

    public void updateMinimumVersion(MinimumVersionUpdateRequest request) {
        OS os = OS.fromLabel(request.os());
        MinimumVersion minimumVersion = minimumVersionRepository.findById(os).orElse(null);

        if (minimumVersion == null) {
            minimumVersion = MinimumVersion.create(os, request.minimumVersion());
        } else {
            minimumVersion.updateMinimumVersion(request.minimumVersion());
        }

        minimumVersionRepository.save(minimumVersion);
    }
}
