package org.websoso.WSSServer.collection.exception;

public class DuplicateCollectionNovelException extends CustomCollectionException {

    public DuplicateCollectionNovelException(CustomCollectionError customCollectionError, String message) {
        super(customCollectionError, message);
    }
}
