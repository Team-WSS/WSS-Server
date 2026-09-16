package org.websoso.WSSServer.collection.exception;

import lombok.Getter;
import org.websoso.common.exception.AbstractCustomException;

@Getter
public class CustomCollectionException extends AbstractCustomException {

    public CustomCollectionException(CustomCollectionError customCollectionError, String message) {
        super(customCollectionError, message);
    }
}
