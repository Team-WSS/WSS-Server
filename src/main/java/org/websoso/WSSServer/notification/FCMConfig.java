package org.websoso.WSSServer.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Slf4j
@Configuration
public class FCMConfig {

    @Value("${fcm.key-path}")
    private String firebaseConfigPath;

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        try {
            GoogleCredentials googleCredentials = GoogleCredentials
                    .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream());

            FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                    .setCredentials(googleCredentials)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(firebaseOptions);
            }

            return FirebaseMessaging.getInstance();
        } catch (IOException e) {
            log.error("[FirebaseMessaging] Failed to initialize FirebaseMessaging", e);
            throw new IllegalStateException("Failed to initialize FirebaseMessaging due to firebase key file", e);
        }
    }
}
