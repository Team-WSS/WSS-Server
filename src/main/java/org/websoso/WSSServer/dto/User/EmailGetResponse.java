package org.websoso.WSSServer.dto.User;

public record EmailGetResponse(
         String email
 ) {

     public static EmailGetResponse of(String email) {
         return new EmailGetResponse(email);
     }
 }
