package org.websoso.WSSServer.exception.error;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.ICustomError;

@AllArgsConstructor
@Getter
public enum CustomGenreError implements ICustomError {

    GENRE_NOT_FOUND("GENRE-001", "해당 이름을 가진 장르를 찾을 수 없습니다.", NOT_FOUND);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
