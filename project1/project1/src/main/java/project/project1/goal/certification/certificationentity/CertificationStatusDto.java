package project.project1.goal.certification.certificationentity;

import lombok.Data;
import project.project1.goal.certification.Certification;
import project.project1.goal.certification.CertificationStatus;
import project.project1.user.SiteUser;

import java.time.LocalDateTime;

//멤버들이 볼 수 있는 그룹의 과제제출 확인란
@Data
public class CertificationStatusDto {
    private Long certificationId;
    private Long userId;
    private String userNickname;
    private CertificationStatus status; // PENDING, APPROVED, REJECTED
    private String certificationType;   // "TEXT", "IMAGE", "SOLVEDAC" 등
    private LocalDateTime createdDate; // 제출 시간
    private LocalDateTime certifiedAt; // 승인 시간

    public static CertificationStatusDto fromEntity(Certification cert) {
        CertificationStatusDto dto = new CertificationStatusDto();
        dto.setCertificationId(cert.getId());

        SiteUser user = cert.getUser();
        if (user != null) {
            dto.setUserId(user.getId());
            dto.setUserNickname(user.getNickname()); // 닉네임 추가
        }

        dto.setStatus(cert.getStatus());
        dto.setCreatedDate(cert.getCreatedDate());
        dto.setCertifiedAt(cert.getCertifiedAt());

        // (중요) 엔티티 타입에 따라 문자열 설정
        if (cert instanceof TextCertification) {
            dto.setCertificationType("TEXT");
        } else if (cert instanceof ImageCertification) {
            dto.setCertificationType("IMAGE");
        } else if (cert instanceof VideoCertification) {
            dto.setCertificationType("VIDEO");
        } else if (cert instanceof SolvedAcCertification) {
            dto.setCertificationType("SOLVEDAC");
        } else if (cert instanceof GithubCertification) {
            dto.setCertificationType("GITHUB");
        } else {
            dto.setCertificationType("UNKNOWN");
        }

        return dto;
    }
}
