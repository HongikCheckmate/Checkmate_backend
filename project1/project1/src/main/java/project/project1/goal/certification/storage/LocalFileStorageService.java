package project.project1.goal.certification.storage;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.project1.goal.certification.CertificationType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service("localFileStorage")
public class LocalFileStorageService implements FileStorageService {
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public String saveFile(MultipartFile file, CertificationType type) {
        try {
            String folder = uploadDir + "/" + type.name().toLowerCase();
            File dir = new File(folder);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filePath = folder + "/" + file.getOriginalFilename();
            file.transferTo(new File(filePath));
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }
}
