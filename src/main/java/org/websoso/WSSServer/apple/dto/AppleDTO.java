package org.websoso.WSSServer.apple.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AppleDTO {

    private String id;
    private String token;
    private String email;
}
