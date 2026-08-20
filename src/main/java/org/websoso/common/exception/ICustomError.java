package org.websoso.common.exception;

import org.springframework.http.HttpStatus;

public interface ICustomError {

    String getCode();

    String getDescription();

    HttpStatus getStatusCode();
}
