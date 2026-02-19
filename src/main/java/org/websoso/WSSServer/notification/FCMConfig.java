package org.websoso.WSSServer.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

@Slf4j
@Configuration
public class FCMConfig {

    @Value("${fcm.key-path}")
    private String firebaseConfigPath;

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        try {
            Resource resource = new FileSystemResource(firebaseConfigPath);
            InputStream inputStream = resource.getInputStream();

            GoogleCredentials googleCredentials = GoogleCredentials
                    .fromStream(inputStream)
                    .createScoped(List.of(
                            "https://www.googleapis.com/auth/firebase.messaging"
                    ));

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
