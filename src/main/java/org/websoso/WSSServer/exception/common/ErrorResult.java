package org.websoso.WSSServer.exception.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResult {

    private String code;
    private String message;
}
