package org.websoso.support.version.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.support.version.domain.MinimumVersion;
import org.websoso.support.version.domain.OS;
import org.websoso.support.version.domain.Version;
import org.websoso.support.version.dto.MinimumVersionUpdateRequest;
import org.websoso.support.version.repository.MinimumVersionRepository;
import org.websoso.support.version.usecase.UpdateAppVersionUseCase;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAppVersionService implements UpdateAppVersionUseCase {

    private final MinimumVersionRepository minimumVersionRepository;

    @Override
    public void updateMinimumVersion(MinimumVersionUpdateRequest request) {
        OS os = OS.fromLabel(request.os());
        Version version = Version.of(request.minimumVersion());

        MinimumVersion minimumVersion = minimumVersionRepository.findById(os).orElse(null);

        if (minimumVersion == null) {
            minimumVersion = MinimumVersion.create(os, version);
        } else {
            minimumVersion.updateMinimumVersion(version);
        }

        minimumVersionRepository.save(minimumVersion);
    }
}
