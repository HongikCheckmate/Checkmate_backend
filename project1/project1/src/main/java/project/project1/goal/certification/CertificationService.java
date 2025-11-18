package project.project1.goal.certification;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.project1.goal.Goal;
import project.project1.goal.GoalService;
import project.project1.goal.certification.certificationentity.*;
import project.project1.goal.certification.external.ExternalCertificationMethod;
import project.project1.goal.certification.external.github.GithubGoal;
import project.project1.goal.certification.external.github.GithubService;
import project.project1.goal.certification.external.github.GithubUser;
import project.project1.goal.certification.external.solvedac.SolvedAcCertificationService;
import project.project1.goal.certification.external.solvedac.SolvedAcGoal;
import project.project1.goal.certification.external.solvedac.SolvedAcUser;
import project.project1.goal.certification.storage.FileStorageService;
import project.project1.goal.certification.storage.ImageGoal;
import project.project1.goal.certification.storage.TextGoal;
import project.project1.goal.certification.storage.VideoGoal;
import project.project1.group.Group;
import project.project1.user.SiteUser;
import project.project1.user.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificationService {

    private final FileStorageService fileStorageService;
    private final CertificationRepository certificationRepository;
    private final GoalService goalService;
    private final UserService userService;
    private final SolvedAcCertificationService solvedAcService;
    private final GithubService githubService;

    @Transactional
    public CertificationManagerDto saveTextCertification(TextCertificationRequest req, Long userId, Long goalId) {
        SiteUser user = userService.findById(userId);
        Goal goal = goalService.findById(goalId);

        if (!(goal instanceof TextGoal)) {
            throw new IllegalArgumentException("이 목표는 텍스트 인증 목표가 아닙니다.");
        }

        TextCertification cert = new TextCertification();
        cert.setUser(user);
        cert.setGoal(goal);
        cert.setContent(req.getContent());
        cert.setStatus(CertificationStatus.PENDING); // 승인 대기

        Certification savedCert = certificationRepository.save(cert);

        return CertificationManagerDto.fromEntity(savedCert);
    }

    @Transactional
    public CertificationManagerDto saveImageCertification(MultipartFile file, Long userId, Long goalId, Long groupId) {
        SiteUser user = userService.findById(userId);
        Goal goal = goalService.findById(goalId);

        if (!(goal instanceof ImageGoal)) {
            throw new IllegalArgumentException("이 목표는 이미지 인증 목표가 아닙니다.");
        }

        String imageUrl = fileStorageService.saveFile(file, CertificationType.IMAGE, groupId, goalId, userId);

        ImageCertification cert = new ImageCertification();
        cert.setUser(user);
        cert.setGoal(goal);
        cert.setImageUrl(imageUrl);
        cert.setStatus(CertificationStatus.PENDING);

        Certification savedCert = certificationRepository.save(cert);

        return CertificationManagerDto.fromEntity(savedCert);
    }

    @Transactional
    public CertificationManagerDto saveVideoCertification(MultipartFile file, Long userId, Long goalId, Long groupId) {
        SiteUser user = userService.findById(userId);
        Goal goal = goalService.findById(goalId);

        if (!(goal instanceof VideoGoal)) {
            throw new IllegalArgumentException("이 목표는 비디오 인증 목표가 아닙니다.");
        }

        String videoUrl = fileStorageService.saveFile(file, CertificationType.VIDEO, groupId, goalId, userId);

        VideoCertification cert = new VideoCertification();
        cert.setUser(user);
        cert.setGoal(goal);
        cert.setVideoUrl(videoUrl);
        cert.setStatus(CertificationStatus.PENDING);

        Certification savedCert = certificationRepository.save(cert);

        // (수정) 엔티티 원본 대신 DTO로 변환하여 반환
        return CertificationManagerDto.fromEntity(savedCert);
    }

    @Transactional
    public CertificationManagerDto certifyExternal(Long userId, Long goalId, ExternalCertificationMethod method) {
        SiteUser user = userService.findById(userId);
        Goal goal = goalService.findById(goalId);
        SolvedAcUser solvedAcUser = user.getSolvedAcUser();
        GithubUser githubUser = user.getGithubUser();
        boolean success = switch (method) {
            case SOLVED_AC -> {
                if (solvedAcUser == null) {
                    throw new IllegalStateException("백준 핸들이 연동되지 않은 사용자입니다.");
                }

                if (!(goal instanceof SolvedAcGoal)) {
                    throw new IllegalArgumentException("이 목표는 Solved.ac 목표가 아닙니다.");
                }

                SolvedAcGoal solvedacGoal = (SolvedAcGoal) goal;
                String handle = solvedAcUser.getHandle();

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
            }
            case GITHUB -> {
                if (githubUser == null) {
                    throw new IllegalStateException("GitHub 계정이 연동되지 않았습니다.");
                }

                if (!(goal instanceof GithubGoal)) {
                    throw new IllegalArgumentException("이 목표는 GitHub 목표가 아닙니다.");
                }

                GithubGoal githubGoal = (GithubGoal) goal;

                yield githubService.verify(githubUser, githubGoal);
            }
        };
        if (!success) {
            throw new IllegalStateException("인증 조건을 충족하지 못했습니다.");
        }

        Certification cert; // 부모 타입으로 선언

        if (method == ExternalCertificationMethod.SOLVED_AC) {
            SolvedAcCertification solvedAcCert = new SolvedAcCertification();
            cert = solvedAcCert;
        } else { // GITHUB
            GithubCertification githubCert = new GithubCertification();
            cert = githubCert;
        }

        cert.setUser(user);
        cert.setGoal(goal);
        cert.setStatus(CertificationStatus.APPROVED); // (중요) 자동 승인
        cert.setCertifiedAt(LocalDateTime.now()); // 승인 시간 설정
        Certification savedCert = certificationRepository.save(cert);

        return CertificationManagerDto.fromEntity(savedCert);
    }

    @Transactional(readOnly = true)
    public Map<Integer, List<CertificationStatusDto>> getCertificationsGroupedByCycle(Goal goal) {
        List<Certification> certifications = certificationRepository.findByGoal(goal);
        LocalDate goalStart = goal.getCreatedDate().toLocalDate();
        int cycleDays = goal.getCycle();

        Map<Integer, List<Certification>> cycleMapTemp = new TreeMap<>();
        for (Certification cert : certifications) {
            LocalDate certDate = cert.getCreatedDate().toLocalDate();
            long daysBetween = ChronoUnit.DAYS.between(goalStart, certDate);
            int cycleIndex = (int) (daysBetween / cycleDays) + 1;
            cycleMapTemp.computeIfAbsent(cycleIndex, k -> new ArrayList<>()).add(cert);
        }

        Map<Integer, List<CertificationStatusDto>> cycleMapDto = new TreeMap<>();
        for (Map.Entry<Integer, List<Certification>> entry : cycleMapTemp.entrySet()) {
            List<CertificationStatusDto> dtoList = entry.getValue().stream()
                    .map(CertificationStatusDto::fromEntity)
                    .collect(Collectors.toList());
            cycleMapDto.put(entry.getKey(), dtoList);
        }
        return cycleMapDto;
    }


    @Transactional(readOnly = true)
    public List<CertificationStatusDto> getCertificationStatusesForGoal(Long goalId, Long currentUserId) {
        // 1. 목표와 그룹 정보 조회
        Goal goal = goalService.findById(goalId);
        Group group = goal.getGroup();
        if (group == null) {
            throw new IllegalStateException("목표에 연결된 그룹이 없습니다.");
        }

        boolean isMember = group.getGroupMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(currentUserId));
        boolean isManager = goal.getManagerId().equals(currentUserId);
        if (!isMember && !isManager) {
            throw new AccessDeniedException("이 그룹의 멤버가 아닙니다.");
        }

        // 3. 멤버가 맞다면, 인증 내역 조회
        List<Certification> certifications = certificationRepository.findByGoal(goal);

        // 4. 민감 정보가 제거된 'CertificationStatusDto'로 변환하여 반환
        return certifications.stream()
                .map(CertificationStatusDto::fromEntity) // DTO의 정적 팩토리 메서드 사용
                .collect(Collectors.toList());
    }


    /**
     * [신규 메서드 2 - 그룹 매니저용]
     * 특정 인증(certificationId) 1개의 '상세 내용'을 반환합니다.
     * 현재 사용자가 해당 목표의 '매니저'인지 확인합니다.
     */
    @Transactional(readOnly = true)
    public CertificationManagerDto getCertificationManager(Long goalId, Long certificationId, Long currentUserId) {

        // 1. (보안 1차) 요청한 Goal의 매니저가 맞는지 확인
        Goal goal = goalService.findById(goalId);
        if (!goal.getManagerId().equals(currentUserId)) {
            throw new AccessDeniedException("당신은 이 목표(ID: " + goalId + ")의 매니저가 아닙니다.");
        }

        // 2. 인증 정보 조회
        Certification cert = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new IllegalArgumentException("인증(ID: " + certificationId + ")을 찾을 수 없습니다."));

        // 3. (보안 2차) 이 인증이 1번에서 검사한 Goal에 속한 것이 맞는지 확인
        if (!cert.getGoal().getId().equals(goalId)) {
            throw new AccessDeniedException("이 인증은 요청한 목표(ID: " + goalId + ")에 속하지 않습니다.");
        }

        // 4. DTO로 변환하여 반환
        return CertificationManagerDto.fromEntity(cert);
    }

    /**
     *
     그룹 매니저용 확인 후 인증 상태 바꾸기(APPROVED, REJECTED)
     */
    @Transactional
    public CertificationManagerDto updateCertificationStatus(
            Long goalId,
            Long certificationId,
            Long currentUserId,
            CertificationUpdateDto updateDto
    ) {

        Goal requestedGoal = goalService.findById(goalId);
        if (!requestedGoal.getManagerId().equals(currentUserId)) {
            throw new AccessDeniedException("이 목표(ID: " + goalId + ")의 매니저가 아닙니다.");
        }

        Certification cert = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new IllegalArgumentException("인증(ID: " + certificationId + ")을 찾을 수 없습니다."));

        if (!cert.getGoal().getId().equals(goalId)) {
            throw new AccessDeniedException("이 인증은 요청한 목표(ID: " + goalId + ")에 속하지 않습니다.");
        }

        if (cert.getStatus() != CertificationStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 인증입니다. (현재 상태: " + cert.getStatus() + ")");
        }

        CertificationStatus newStatus = updateDto.getNewStatus();

        if (newStatus == CertificationStatus.APPROVED) {
            cert.setStatus(CertificationStatus.APPROVED);
            cert.setCertifiedAt(LocalDateTime.now());
        } else if (newStatus == CertificationStatus.REJECTED) {
            cert.setStatus(CertificationStatus.REJECTED);
            cert.setCertifiedAt(null);
        } else {
            throw new IllegalArgumentException("변경 요청 상태가 올바르지 않습니다. (APPROVED 또는 REJECTED만 가능)");
        }

        return CertificationManagerDto.fromEntity(cert);
    }

}
