package org.websoso.WSSServer.library.domain;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

public class ParentTouchListener {
    @PostPersist
    @PostRemove
    @PostUpdate
    public void touchParent(Object child) {
        if (child instanceof UserNovelKeyword uk) {
            uk.getUserNovel().touch();
        } else if (child instanceof UserNovelAttractivePoint ap) {
            ap.getUserNovel().touch();
        }
    }
}
