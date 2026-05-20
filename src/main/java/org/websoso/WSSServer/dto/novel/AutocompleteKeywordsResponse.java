package org.websoso.WSSServer.dto.novel;

import java.util.List;

public record AutocompleteKeywordsResponse(
        List<String> keywords
) {
}
