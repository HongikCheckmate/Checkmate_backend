package project.project1.certification;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import project.project1.goal.certification.CertificationType;
import project.project1.goal.certification.storage.FileStorageService;
import project.project1.goal.certification.storage.S3FileStorageService;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.UUID;

public class S3FileUploadTest {
    private static final Logger log = LoggerFactory.getLogger(S3FileUploadTest.class);

    void S3_업로드() throws Exception {
        String bucket = System.getenv().getOrDefault("STORAGE_S3_BUCKET", "hongik-checkmate");
        String region = System.getenv().getOrDefault("AWS_REGION", "ap-northeast-2");
        S3Client s3 = S3Client.builder()
                .region(Region.of(region))
                .build();



        String key = "test/" + UUID.randomUUID() + ".png";
        log.info("Check key : " + key);
        try (InputStream is = new ClassPathResource("test-image.png").getInputStream()) {
            s3.putObject(
                    PutObjectRequest.builder().bucket(bucket).key(key).contentType("image/png").build(),
                    RequestBody.fromBytes(is.readAllBytes())
            );
        }
        System.out.println("[Success] uploaded s3://" + bucket + "/" + key);
        Assertions.assertTrue(key.startsWith("test/"));
    }



}
