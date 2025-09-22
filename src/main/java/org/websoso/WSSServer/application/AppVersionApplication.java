package org.websoso.WSSServer.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.support.version.dto.MinimumVersionGetResponse;
import org.websoso.support.version.dto.MinimumVersionUpdateRequest;
import org.websoso.support.version.usecase.GetAppVersionQuery;
import org.websoso.support.version.usecase.UpdateAppVersionUseCase;

@Service
@RequiredArgsConstructor
public class AppVersionApplication {

    private final GetAppVersionQuery getAppVersionQuery;
    private final UpdateAppVersionUseCase updateAppVersionUseCase;

    public MinimumVersionGetResponse getMinimumVersion(String os) {
        return getAppVersionQuery.getMinimumVersion(os);
    }

    public void updateMinimumVersion(MinimumVersionUpdateRequest request) {
        updateAppVersionUseCase.updateMinimumVersion(request);
    }
}
