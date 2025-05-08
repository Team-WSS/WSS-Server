package org.websoso.WSSServer.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.websoso.s3.config.S3AccessConfig;
import org.websoso.s3.core.S3FileService;
import org.websoso.s3.factory.S3ClientFactory;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@Configuration
public class S3Config {

    @Value("${s3.access-key}")
    private String accessKey;

    @Value("${s3.secret-key}")
    private String secretKey;

    @Value("${s3.bucket}")
    private String bucket;

    @Bean
    public S3AccessConfig s3AccessConfig() {
        return S3AccessConfig.builder()
                .withCredentials(accessKey, secretKey)
                .build();
    }

    @Bean
    public S3Client s3Client() {
        return S3ClientFactory.getS3Client(s3AccessConfig());
    }

    @Bean
    public S3FileService s3FileService() {
        return new S3FileService(s3Client(), bucket);
    }

    @PostConstruct
    public void printKeys() {
        log.info("AccessKey = " + accessKey);
        log.info("SecretKey = " + secretKey);
        log.info("Bucket = " + bucket);
    }

}
