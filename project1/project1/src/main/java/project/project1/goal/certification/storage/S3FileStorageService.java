package project.project1.goal.certification.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.project1.goal.certification.CertificationType;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile({"prod", "s3-it"})
public class S3FileStorageService implements FileStorageService {

    @Value("${storage.s3.bucket:}")
    private String bucket;

    @Value("${storage.s3.base-prefix:")
    private String basePrefix;

    private final S3ClientHolder s3ClientHolder;

    @Override
    public String saveFile(MultipartFile file, CertificationType type, Long groupId, Long goalId, Long userId) {

        try{
            // 2025-11-03
            String datePath = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

            // 파일 이름, 확장자 분리
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if(originalFilename != null && originalFilename.lastIndexOf('.') >= 0){
                extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }

            // 랜덤 UUID 부여로 저장 (UUID.png)
            String savedFileName = UUID.randomUUID() + extension;

            String relativeKey = String.format("group_%d/goal_%d/%s/user_%d/%s", groupId, goalId, datePath, userId, savedFileName);
            String s3Key = (basePrefix == null || basePrefix.isBlank()) ? relativeKey : trimSlash(basePrefix) + "/" + relativeKey;

            PutObjectRequest.Builder putRequestBuilder = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .contentType(file.getContentType());

            s3ClientHolder.client().putObject(
                    putRequestBuilder.build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            log.info("Saved certification file to S3 : bucket={} key={} size={}", bucket, s3Key, file.getSize());

            return s3Key;

        } catch(IOException io){
            throw new RuntimeException("Error saving certification file on S3", io);
        }
    }

    private String trimSlash(String s) {
        if (s == null) return null;
        if (s.startsWith("/")) s = s.substring(1);
        if (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s;
    }

    @Service
    static class S3ClientHolder {
        private final S3Client client;

        public S3ClientHolder(@Value("${storage.s3.region:ap-northeast-2}") String region) {
            this.client = S3Client.builder().region(Region.of(region)).build();
        }

        public S3Client client() { return client; }
    }
}
