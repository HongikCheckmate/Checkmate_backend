package project.project1.goal.certification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.project1.goal.Goal;
import project.project1.goal.GoalService;
import project.project1.goal.certification.external.ExternalCertificationMethod;
import project.project1.goal.certification.external.solvedac.GoalMember;
import project.project1.goal.certification.external.solvedac.SolvedAcCertificationService;
import project.project1.goal.certification.storage.FileStorageService;
import project.project1.user.SiteUser;
import project.project1.user.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class CertificationService {

    private final FileStorageService fileStorageService;
    private final CertificationRepository certificationRepository;
    private final GoalService goalService;
    private final UserService userService;
    private final SolvedAcCertificationService solvedAcService;

    public Certification saveTextCertification(TextCertificationRequest req, Long userId, Long goalId) {
        SiteUser user = userService.findById(userId);
        Goal goal = goalService.findById(goalId);

        Certification cert = new Certification();
        cert.setUser(user);
        cert.setGoal(goal);
        cert.setType(CertificationType.TEXT);
        cert.setContent(req.getContent());
        cert.setCertifiedAt(LocalDateTime.now());

        return certificationRepository.save(cert);
    }

    public Certification saveImageCertification(MultipartFile file, Long userId, Long goalId, Long groupId) {
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

        return certificationRepository.save(cert);
    }

    public Certification saveVideoCertification(MultipartFile file, Long userId, Long goalId, Long groupId) {
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

        return certificationRepository.save(cert);
    }

    public Certification certifyExternal(Long userId, Long goalId, ExternalCertificationMethod method) {
        SiteUser user = userService.findById(userId);
        String handle = user.getSolvedAcHandle();
        Goal goal = goalService.findById(goalId);

        boolean success = switch (method) {
            case SOLVED_AC -> {
                yield switch (goal.getProblemGoalType()) {

                    // [개선] startCount를 찾는 복잡한 로직이 사라지고, Goal의 메서드 호출로 변경됨
                    case COUNT -> solvedAcService.verifyNProblemsSolvedInPeriod(
                            handle,
                            goal.getProblemCount(),
                            goal.getStartCountForUser(userId) // Goal의 헬퍼 메서드 사용
                    );
                    case SPECIFIC -> solvedAcService.verifySpecificProblemsSolved(
                            handle,
                            goal.getTargetProblemIds()
                    );
                };
            }
//        case GITHUB -> githubService.verify(handle, goal); // 추후 구현
        };
        if (!success) {
            throw new IllegalStateException("인증 조건을 충족하지 못했습니다.");
        }

        Certification cert = new Certification();
        cert.setUser(user);
        cert.setGoal(goal);
        cert.setType(CertificationType.EXTERNAL);
        cert.setMethod(method);
        cert.setCertifiedAt(LocalDateTime.now());

        return certificationRepository.save(cert);
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


    public List<Certification> getUserCertifications(Long goalId, Long userId) {
        return certificationRepository.findByGoalIdAndUserId(goalId, userId);
    }
}
