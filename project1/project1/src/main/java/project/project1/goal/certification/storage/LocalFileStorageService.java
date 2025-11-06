package project.project1.goal.certification.storage;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.project1.goal.certification.CertificationType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service("localFileStorage")
@Profile({"dev", "default"})
public class LocalFileStorageService implements FileStorageService {
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public String saveFile(MultipartFile file, CertificationType type, Long groupId, Long goalId, Long userId) {
        try {
            // 날짜 기반 디렉토리 (주기 단위 구분 가능)
            String datePath = LocalDate.now().format(DateTimeFormatter.ISO_DATE); // ex. 2025-05-14

            // 디렉토리 경로 구성
            String folderPath = String.format("%s/group_%d/goal_%d/%s/user_%d",
                    uploadDir, groupId, goalId, datePath, userId);

            File dir = new File(folderPath);
            if (!dir.exists()) {
                dir.mkdirs(); // 디렉토리 없으면 생성
            }

            // 파일 이름 구성 (UUID로 충돌 방지)
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String savedFileName = UUID.randomUUID() + extension;

            // 전체 파일 경로
            File dest = new File(dir, savedFileName);
            file.transferTo(dest);

            //상대 경로 반환
            return String.format("group_%d/goal_%d/%s/user_%d/%s", groupId, goalId, datePath, userId, savedFileName);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }
}
