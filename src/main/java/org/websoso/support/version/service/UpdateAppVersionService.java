package org.websoso.support.version.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.support.version.domain.MinimumVersion;
import org.websoso.support.version.dto.UpdateMinimumVersionCommand;
import org.websoso.support.version.repository.MinimumVersionRepository;
import org.websoso.support.version.usecase.UpdateAppVersionUseCase;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAppVersionService implements UpdateAppVersionUseCase {

    private final MinimumVersionRepository minimumVersionRepository;

    @Override
    public void updateMinimumVersion(UpdateMinimumVersionCommand command) {
        MinimumVersion minimumVersion = minimumVersionRepository.findById(command.os()).orElse(null);

        if (minimumVersion == null) {
            minimumVersion = MinimumVersion.create(command.os(), command.version());
        } else {
            minimumVersion.updateMinimumVersion(command.version());
        }

        minimumVersionRepository.save(minimumVersion);
    }
}
