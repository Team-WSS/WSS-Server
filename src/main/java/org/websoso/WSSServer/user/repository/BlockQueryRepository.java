package org.websoso.WSSServer.user.repository;

import java.util.List;
import org.websoso.WSSServer.user.repository.projection.BlockInfoRow;

public interface BlockQueryRepository {

    List<BlockInfoRow> findBlockInfoRows(Long blockingId);
}
