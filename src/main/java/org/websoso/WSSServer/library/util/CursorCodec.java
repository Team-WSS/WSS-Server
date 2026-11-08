package org.websoso.WSSServer.library.util;

import static org.websoso.WSSServer.exception.error.CustomFilteringError.INVALID_CURSOR;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.exception.exception.CustomFilteringException;
import org.websoso.WSSServer.library.repository.cursor.UserNovelCursor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class CursorCodec {

    private final ObjectMapper objectMapper;

    public String encode(UserNovelCursor cursor) {
        if (cursor == null) {
            return null;
        }

        try {
            String json = objectMapper.writeValueAsString(cursor);
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (JacksonException e) {
            throw new CustomFilteringException(INVALID_CURSOR, "failed to encode user novel cursor");
        }
    }

    public UserNovelCursor decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cursor);
            return objectMapper.readValue(decoded, UserNovelCursor.class);
        } catch (IllegalArgumentException | JacksonException e) {
            throw new CustomFilteringException(INVALID_CURSOR, "given user novel cursor is invalid");
        }
    }
}
