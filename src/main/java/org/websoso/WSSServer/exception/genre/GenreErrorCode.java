package org.websoso.WSSServer.exception.genre;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.IErrorCode;

@AllArgsConstructor
@Getter
public enum GenreErrorCode implements IErrorCode {

    GENRE_NOT_FOUND("GENRE-001", "해당하는 장르를 찾을 수 없습니다.", NOT_FOUND);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
