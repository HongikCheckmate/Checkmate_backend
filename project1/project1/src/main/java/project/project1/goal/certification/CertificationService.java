package project.project1.goal.certification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.project1.goal.Goal;
import project.project1.goal.GoalRepository;
import project.project1.goal.GoalService;
import project.project1.goal.certification.storage.FileStorageService;
import project.project1.user.SiteUser;
import project.project1.user.UserRepository;
import project.project1.user.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class CertificationService {

    private final FileStorageService fileStorageService;
    private final CertificationRepository certificationRepository;
    private final GoalService goalService;
    private final UserService userService;

    public CertificationService(CertificationRepository cr,
                                @Qualifier("localFileStorage") FileStorageService fs,
                                GoalService gs,
                                UserService us) {
        this.certificationRepository = cr;
        this.fileStorageService = fs;
        this.goalService = gs;
        this.userService = us;
    }

    public void saveTextCertification(TextCertificationRequest request, Long userId, Long goalId) {
        SiteUser user = userService.findById(userId);
        Goal goal = goalService.findById(goalId);

        Certification cert = new Certification();
        cert.setUser(user);
        cert.setGoal(goal);
        cert.setType(CertificationType.TEXT);
        cert.setContent(request.getContent());
        cert.setCertifiedAt(LocalDateTime.now());

        certificationRepository.save(cert);
    }

    public void saveImageCertification(MultipartFile file, Long userId, Long goalId, Long groupId) {
        SiteUser user = userService.findById(userId);
        Goal goal = goalService.findById(goalId);
        // 이미지 파일 저장 로직 (예: S3, 로컬 경로 등)
        String imageUrl = fileStorageService.saveFile(file, CertificationType.IMAGE, groupId, goalId, userId);
        Certification cert = new Certification();
        cert.setUser(user);
        cert.setGoal(goal);
        cert.setType(CertificationType.IMAGE);
        cert.setContentUrl(imageUrl);
        cert.setCertifiedAt(LocalDateTime.now());

        certificationRepository.save(cert);
    }

    public void saveVideoCertification(MultipartFile file, Long userId, Long goalId, Long groupId) {
        SiteUser user = userService.findById(userId);
        Goal goal = goalService.findById(goalId);
        // 동영상 저장 로직
        String videoUrl = fileStorageService.saveFile(file, CertificationType.VIDEO, groupId, goalId, userId);
        Certification cert = new Certification();
        cert.setUser(user);
        cert.setGoal(goal);
        cert.setType(CertificationType.VIDEO);
        cert.setContentUrl(videoUrl);
        cert.setCertifiedAt(LocalDateTime.now());

        certificationRepository.save(cert);
    }

    private String saveFile(MultipartFile file) {
        // 실제 파일 저장 로직
        return "https://example.com/fake-path/" + file.getOriginalFilename();
    }


    public Map<Integer, List<Certification>> getCertificationsGroupedByCycle(Goal goal) {
        List<Certification> certifications = certificationRepository.findByGoal(goal);
        LocalDate goalStart = goal.getCreatedDate().toLocalDate(); // 목표 생성일
        int cycleDays = goal.getCycle(); // 주기: 3일, 7일 등

        Map<Integer, List<Certification>> cycleMap = new TreeMap<>();

        for (Certification cert : certifications) {
            LocalDate certDate = cert.getCreatedDate().toLocalDate();

            long daysBetween = ChronoUnit.DAYS.between(goalStart, certDate);
            int cycleIndex = (int) (daysBetween / cycleDays) + 1; // 1부터 시작

            cycleMap.computeIfAbsent(cycleIndex, k -> new ArrayList<>()).add(cert);
        }

        return cycleMap;
    }
}
