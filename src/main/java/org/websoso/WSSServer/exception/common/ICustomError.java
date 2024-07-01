package org.websoso.WSSServer.exception.common;

import org.springframework.http.HttpStatus;

public interface ICustomError {

    String getCode();

    String getDescription();

    HttpStatus getStatusCode();
}
