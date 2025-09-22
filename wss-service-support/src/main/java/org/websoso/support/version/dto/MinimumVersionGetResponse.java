package org.websoso.support.version.dto;

import java.time.format.DateTimeFormatter;
import org.websoso.support.version.domain.MinimumVersion;

public record MinimumVersionGetResponse(
        String minimumVersion,
        String updateDate
) {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static MinimumVersionGetResponse of(MinimumVersion minimumVersion) {
        return new MinimumVersionGetResponse(
                minimumVersion.getMinimumVersion(),
                minimumVersion.getUpdateDate().format(FORMATTER)
        );
    }
}
