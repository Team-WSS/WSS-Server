package org.websoso.support.version.usecase;


import org.websoso.support.version.dto.UpdateMinimumVersionCommand;

public interface UpdateAppVersionUseCase {
    void updateMinimumVersion(UpdateMinimumVersionCommand command);
}
