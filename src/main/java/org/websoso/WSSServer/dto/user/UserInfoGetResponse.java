package org.websoso.WSSServer.dto.user;

public record UserInfoGetResponse(
         String email
 ) {
     public static UserInfoGetResponse of(String email) {
         return new UserInfoGetResponse(email);
     }
 }
