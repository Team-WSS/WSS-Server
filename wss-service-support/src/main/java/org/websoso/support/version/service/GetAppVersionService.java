package org.websoso.support.version.service;

import static org.websoso.support.version.exception.CustomMinimumVersionError.MINIMUM_VERSION_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.support.version.domain.OS;
import org.websoso.support.version.dto.GetMinimumVersionResult;
import org.websoso.support.version.exception.CustomMinimumVersionException;
import org.websoso.support.version.repository.MinimumVersionRepository;
import org.websoso.support.version.usecase.GetAppVersionQuery;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAppVersionService implements GetAppVersionQuery {

    private final MinimumVersionRepository minimumVersionRepository;

    @Override
    public GetMinimumVersionResult getMinimumVersion(OS os) {
        return GetMinimumVersionResult.of(minimumVersionRepository.findById(os)
                .orElseThrow(() -> new CustomMinimumVersionException(MINIMUM_VERSION_NOT_FOUND,
                        "the minimum supported version for the specified OS could not be found")));
    }
}
