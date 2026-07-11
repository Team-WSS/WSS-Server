package org.websoso.WSSServer.library.util;

import static org.websoso.WSSServer.exception.error.CustomFilteringError.INVALID_CURSOR;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.exception.exception.CustomFilteringException;
import org.websoso.WSSServer.library.repository.cursor.UserNovelCursor;

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
        } catch (JsonProcessingException e) {
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
        } catch (IllegalArgumentException | IOException e) {
            throw new CustomFilteringException(INVALID_CURSOR, "given user novel cursor is invalid");
        }
    }
}
