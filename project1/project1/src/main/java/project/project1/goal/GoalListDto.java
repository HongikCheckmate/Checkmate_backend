package project.project1.goal;

import lombok.Builder;
import lombok.Getter;
import project.project1.goal.certification.CertificationType;
import project.project1.goal.certification.external.ExternalCertificationMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
public class GoalListDto {
    private Long id;
    private String name;
    private LocalDate endDate;
    private CertificationType certificationType;
    private ExternalCertificationMethod externalMethod;
    private Long groupId;
    private int cycle;
    private LocalDateTime createdDate;

        // (만약 SolvedAcGoal의 problemGoalType도 목록에 필요하다면
        //  instanceof로 확인해서 DTO 필드에 추가할 수 있습니다.)
}
