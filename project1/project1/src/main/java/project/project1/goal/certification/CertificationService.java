package project.project1.goal.certification;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.project1.goal.certification.storage.FileStorageService;

import java.time.LocalDateTime;

@Service
public class CertificationService {
    private final CertificationRepository certificationRepository;
    private final FileStorageService fileStorageService;

    public CertificationService(CertificationRepository cr, @Qualifier("localFileStorage") FileStorageService fs) {
        this.certificationRepository = cr;
        this.fileStorageService = fs;
    }

    public void saveTextCertification(TextCertificationRequest request) {
        Certification cert = new Certification();
        cert.setUserId(request.getUserId());
        cert.setGoalId(request.getGoalId());
        cert.setType(CertificationType.TEXT);
        cert.setContent(request.getContent());
        cert.setCertifiedAt(LocalDateTime.now());

        certificationRepository.save(cert);
    }

    public void saveImageCertification(MultipartFile file, Long userId, Long goalId) {
        // 이미지 파일 저장 로직 (예: S3, 로컬 경로 등)
        String imageUrl = fileStorageService.saveFile(file, CertificationType.IMAGE);
        Certification cert = new Certification();
        cert.setUserId(userId);
        cert.setGoalId(goalId);
        cert.setType(CertificationType.IMAGE);
        cert.setContentUrl(imageUrl);
        cert.setCertifiedAt(LocalDateTime.now());

        certificationRepository.save(cert);
    }

    public void saveVideoCertification(MultipartFile file, Long userId, Long goalId) {
        // 동영상 저장 로직
        String videoUrl = fileStorageService.saveFile(file, CertificationType.VIDEO);
        Certification cert = new Certification();
        cert.setUserId(userId);
        cert.setGoalId(goalId);
        cert.setType(CertificationType.VIDEO);
        cert.setContentUrl(videoUrl);
        cert.setCertifiedAt(LocalDateTime.now());

        certificationRepository.save(cert);
    }

    private String saveFile(MultipartFile file) {
        // 실제 파일 저장 로직
        return "https://example.com/fake-path/" + file.getOriginalFilename();
    }
}
