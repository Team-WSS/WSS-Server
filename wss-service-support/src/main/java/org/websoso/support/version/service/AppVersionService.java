package org.websoso.support.version.service;

import static org.websoso.support.version.exception.CustomMinimumVersionError.MINIMUM_VERSION_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.support.version.domain.MinimumVersion;
import org.websoso.support.version.domain.OS;
import org.websoso.support.version.dto.MinimumVersionGetResponse;
import org.websoso.support.version.dto.MinimumVersionUpdateRequest;
import org.websoso.support.version.exception.CustomMinimumVersionException;
import org.websoso.support.version.repository.MinimumVersionRepository;

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
