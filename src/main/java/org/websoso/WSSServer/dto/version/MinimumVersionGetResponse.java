package org.websoso.WSSServer.dto.version;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.websoso.support.version.domain.Version;

public record MinimumVersionGetResponse(
        String minimumVersion,
        String updateDate
) {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static MinimumVersionGetResponse of(Version version, LocalDateTime updateDate) {
        return new MinimumVersionGetResponse(
                version.getValue(),
                updateDate.format(FORMATTER)
        );
    }
}
