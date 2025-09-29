package org.websoso.support.version.dto;

import java.time.LocalDateTime;
import org.websoso.support.version.domain.MinimumVersion;
import org.websoso.support.version.domain.Version;

public record GetMinimumVersionResult(
        Version version,
        LocalDateTime updateDate
) {
    public static GetMinimumVersionResult of(MinimumVersion minimumVersion) {
        return new GetMinimumVersionResult(
                minimumVersion.getMinimumVersion(),
                minimumVersion.getUpdateDate()
        );
    }
}
