package project.project1.goal.certification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.project1.goal.Goal;
import project.project1.goal.GoalService;
import project.project1.goal.certification.external.ExternalCertificationMethod;
import project.project1.goal.certification.external.github.GithubGoal;
import project.project1.goal.certification.external.github.GithubService;
import project.project1.goal.certification.external.github.GithubUser;
import project.project1.goal.certification.external.solvedac.SolvedAcCertificationService;
import project.project1.goal.certification.external.solvedac.SolvedAcGoal;
import project.project1.goal.certification.external.solvedac.SolvedAcUser;
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
    private final GithubService githubService;

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
        Goal goal = goalService.findById(goalId);
        SolvedAcUser solvedAcUser = user.getSolvedAcUser();
        GithubUser githubUser = user.getGithubUser();
        boolean success = switch (method) {
            case SOLVED_AC -> {
                if (solvedAcUser == null) {
                    throw new IllegalStateException("백준 핸들이 연동되지 않은 사용자입니다.");
                }

                // --- (수정) 타입 확인 및 형변환 ---
                if (!(goal instanceof SolvedAcGoal)) {
                    throw new IllegalArgumentException("이 목표는 Solved.ac 목표가 아닙니다.");
                }
                // 2. 자식 타입으로 형변환
                SolvedAcGoal solvedacGoal = (SolvedAcGoal) goal;
                String handle = solvedAcUser.getHandle();

                // 3. 자식 타입(SolvedacGoal)의 메서드 사용
                yield switch (solvedacGoal.getProblemGoalType()) {
                    case COUNT -> solvedAcService.verifyNProblemsSolvedInPeriod(
                            handle,
                            solvedacGoal.getProblemCount(),         // solvedacGoal.get...
                            solvedacGoal.getStartCountForUser(userId) // solvedacGoal.get...
                    );
                    case SPECIFIC -> solvedAcService.verifySpecificProblemsSolved(
                            handle,
                            solvedacGoal.getTargetProblemIds()    // solvedacGoal.get...
                    );
                };
                // ---------------------------------
            }
            case GITHUB -> {
                if (githubUser == null) {
                    throw new IllegalStateException("GitHub 계정이 연동되지 않았습니다.");
                }

                // --- (수정) 타입 확인 및 형변환 ---
                if (!(goal instanceof GithubGoal)) {
                    throw new IllegalArgumentException("이 목표는 GitHub 목표가 아닙니다.");
                }
                // 2. 자식 타입으로 형변환
                GithubGoal githubGoal = (GithubGoal) goal;

                // 3. (중요) GithubService의 verify 메서드도 GithubGoal 타입을 받도록 수정해야 함
                yield githubService.verify(githubUser, githubGoal);
                // ---------------------------------
            }
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
