package org.websoso.WSSServer.dto.user;

import org.websoso.WSSServer.domain.User;

public record UserInfoGetResponse(
         String email,
         String gender,
         Integer birth
 ) {
     public static UserInfoGetResponse of(User user) {
         return new UserInfoGetResponse(
                 user.getEmail(),
                 user.getGender().name(),
                 user.getBirth().getValue()
         );
     }
 }
