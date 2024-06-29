package org.websoso.WSSServer.exception.genre.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.genre.GenreErrorCode;

@Getter
@AllArgsConstructor
public class InvalidGenreException extends RuntimeException {

    public InvalidGenreException(GenreErrorCode genreErrorCode, String message) {
        super(message);
        this.genreErrorCode = genreErrorCode;
    }

    private GenreErrorCode genreErrorCode;
}
