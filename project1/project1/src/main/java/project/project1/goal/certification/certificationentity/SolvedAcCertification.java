package project.project1.goal.certification.certificationentity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.project1.goal.certification.Certification;

@Entity
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("SOLVED_AC")
public class SolvedAcCertification extends Certification {
    // Specific 모드 인증 시, 푼 문제 ID
    private Long problemId;

    // Count 모드 인증 시, 몇 개를 풀었는지 (선택적)
    private Integer solvedCount;
}
