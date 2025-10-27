package project.project1.goal;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import project.project1.goal.certification.CertificationType;
import project.project1.goal.certification.external.ExternalCertificationMethod;
import project.project1.goal.certification.external.ProblemGoalType;

import java.time.LocalDate;

@Data
@Getter
@Setter
public class GoalCreateRequestDto {
    private String name;
    private int cycle;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private Long groupId; // @RequestParam 대신 DTO에 포함

    // --- 인증 방식 결정 필드 ---
    private CertificationType certificationType;
    private ExternalCertificationMethod externalMethod;

    // --- Solved.ac 전용 필드 ---
    private ProblemGoalType problemGoalType;
    private Integer problemCount;

    // (참고) SPECIFIC 모드일 때, 폼에서 "1001,1002,1003" 처럼
    // 쉼표로 구분된 문자열로 문제 ID를 받는다고 가정합니다.
    private String targetProblemIds;

    // --- GitHub 전용 필드 ---
    private String targetRepository;
}
