package project.project1.goal.certification.storage;

import org.springframework.web.multipart.MultipartFile;
import project.project1.goal.certification.CertificationType;

public interface FileStorageService {
    String saveFile(MultipartFile file, CertificationType type);
}
