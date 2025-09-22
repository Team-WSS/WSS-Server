package org.websoso.support.version.usecase;

import org.websoso.support.version.dto.MinimumVersionUpdateRequest;

public interface UpdateAppVersionUseCase {
    void updateMinimumVersion(MinimumVersionUpdateRequest request);
}
