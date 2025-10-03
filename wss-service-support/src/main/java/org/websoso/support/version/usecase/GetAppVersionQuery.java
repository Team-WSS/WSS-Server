package org.websoso.support.version.usecase;

import org.websoso.support.version.domain.OS;
import org.websoso.support.version.dto.GetMinimumVersionResult;

public interface GetAppVersionQuery {
    GetMinimumVersionResult getMinimumVersion(OS os);
}
