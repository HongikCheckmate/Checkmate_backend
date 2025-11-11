package project.project1.goal.certification.certificationentity;

import lombok.Data;
import project.project1.goal.Goal;
import project.project1.goal.certification.Certification;
import project.project1.goal.certification.CertificationStatus;
import project.project1.user.SiteUser;

import java.time.LocalDateTime;

@Data
public class CertificationManagerDto {
    private Long certificationId;
    private CertificationStatus status;
    private LocalDateTime createdDate; // 제출 시간
    private LocalDateTime certifiedAt; // 승인 시간
    private String rejectReason;

    // 2. 제출자 정보 (민감 정보 제외)
    private Long userId;
    private String userNickname;

    // 3. 목표 정보 (민감 정보 제외)
    private Long goalId;
    private String goalName;

    // 4. (핵심) 인증 타입 및 상세 내용
    private String certificationType;

    // --- 민감 정보 (Manager only) ---
    private String content;      // for TextCertification
    private String imageUrl;     // for ImageCertification
    private String videoUrl;     // for VideoCertification
    private Long problemId;      // for SolvedacCertification
    private String commitSha;    // for GithubCertification
    // ---

    /**
     * 전체 Certification 엔티티를 받아서 상세 DTO로 변환하는 정적 팩토리 메서드
     * (이 메서드는 Service의 @Transactional 안에서 호출되어야 함)
     */
    public static CertificationManagerDto fromEntity(Certification cert) {
        CertificationManagerDto dto = new CertificationManagerDto();

        // 1. 공통 필드 매핑
        dto.setCertificationId(cert.getId());
        dto.setStatus(cert.getStatus());
        dto.setCreatedDate(cert.getCreatedDate());
        dto.setCertifiedAt(cert.getCertifiedAt());

        // 2. User 정보 매핑 (LAZY 로딩 초기화)
        SiteUser user = cert.getUser();
        if (user != null) {
            dto.setUserId(user.getId());
            dto.setUserNickname(user.getNickname());
        }

        // 3. Goal 정보 매핑 (LAZY 로딩 초기화)
        Goal goal = cert.getGoal();
        if (goal != null) {
            dto.setGoalId(goal.getId());
            dto.setGoalName(goal.getName());
        }

        // 4. (핵심) 자식 엔티티 타입에 따라 상세 내용 및 타입 매핑
        if (cert instanceof TextCertification) {
            dto.setCertificationType("TEXT");
            dto.setContent(((TextCertification) cert).getContent());

        } else if (cert instanceof ImageCertification) {
            dto.setCertificationType("IMAGE");
            dto.setImageUrl(((ImageCertification) cert).getImageUrl());

        } else if (cert instanceof VideoCertification) {
            dto.setCertificationType("VIDEO");
            dto.setVideoUrl(((VideoCertification) cert).getVideoUrl());

        } else if (cert instanceof SolvedAcCertification) {
            dto.setCertificationType("SOLVED_AC");
            dto.setProblemId(((SolvedAcCertification) cert).getProblemId());

        } else if (cert instanceof GithubCertification) {
            dto.setCertificationType("GITHUB");
            dto.setCommitSha(((GithubCertification) cert).getCommitSha());

        } else {
            dto.setCertificationType("UNKNOWN");
        }

        return dto;
    }
}
