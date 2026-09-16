package org.websoso.WSSServer.dto.user;

import org.websoso.WSSServer.user.domain.User;
import org.websoso.support.logging.masking.MaskingPolicy;
import org.websoso.support.logging.masking.SensitiveData;

public record UserInfoGetResponse(
         @SensitiveData(MaskingPolicy.EMAIL)
         String email,
         @SensitiveData(MaskingPolicy.FULL)
         String gender,
         @SensitiveData(MaskingPolicy.FULL)
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
