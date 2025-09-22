package org.websoso.support.version.usecase;

import org.websoso.support.version.dto.MinimumVersionGetResponse;

public interface GetAppVersionQuery {
    MinimumVersionGetResponse getMinimumVersion(String os);
}
