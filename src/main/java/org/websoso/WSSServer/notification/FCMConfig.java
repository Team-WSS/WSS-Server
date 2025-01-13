package org.websoso.WSSServer.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FCMConfig {

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource("websoso-fcm.json").getInputStream());

        FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                .setCredentials(googleCredentials)
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(firebaseOptions);
        }

        return FirebaseMessaging.getInstance();
    }
}
