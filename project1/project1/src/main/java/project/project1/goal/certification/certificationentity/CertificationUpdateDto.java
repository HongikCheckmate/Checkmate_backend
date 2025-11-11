package project.project1.goal.certification.certificationentity;

import lombok.Data;
import project.project1.goal.certification.CertificationStatus;

@Data
public class CertificationUpdateDto {

    // "APPROVED" 또는 "REJECTED"
    private CertificationStatus newStatus;

    // REJECTED(반려)일 경우의 사유
    private String rejectReason;
}