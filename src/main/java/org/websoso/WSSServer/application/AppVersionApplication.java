package org.websoso.WSSServer.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.dto.version.MinimumVersionGetResponse;
import org.websoso.WSSServer.dto.version.MinimumVersionUpdateRequest;
import org.websoso.support.version.domain.OS;
import org.websoso.support.version.dto.GetMinimumVersionResult;
import org.websoso.support.version.dto.UpdateMinimumVersionCommand;
import org.websoso.support.version.usecase.GetAppVersionQuery;
import org.websoso.support.version.usecase.UpdateAppVersionUseCase;

@Service
@RequiredArgsConstructor
public class AppVersionApplication {

    private final GetAppVersionQuery getAppVersionQuery;
    private final UpdateAppVersionUseCase updateAppVersionUseCase;

    public MinimumVersionGetResponse getMinimumVersion(String os) {
        GetMinimumVersionResult result = getAppVersionQuery.getMinimumVersion(OS.fromLabel(os));
        return MinimumVersionGetResponse.of(result.version(), result.updateDate());
    }

    public void updateMinimumVersion(MinimumVersionUpdateRequest request) {
        UpdateMinimumVersionCommand command = UpdateMinimumVersionCommand.of(request.os(), request.minimumVersion());
        updateAppVersionUseCase.updateMinimumVersion(command);
    }
}
