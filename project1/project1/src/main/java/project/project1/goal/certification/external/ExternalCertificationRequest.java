package project.project1.goal.certification.external;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalCertificationRequest {
    private Long goalId;   // 목표 ID
    private ExternalCertificationMethod method;
}
