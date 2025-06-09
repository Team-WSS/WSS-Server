package org.websoso.WSSServer.domain.common;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.websoso.WSSServer.domain.UserNovelAttractivePoint;
import org.websoso.WSSServer.domain.UserNovelKeyword;

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
