package org.websoso.WSSServer.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FCMConfig {

    @Bean
    public FirebaseMessaging firebaseMessaging() {

    }
}
