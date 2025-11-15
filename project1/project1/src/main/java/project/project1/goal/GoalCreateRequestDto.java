package project.project1.goal;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import project.project1.goal.certification.CertificationType;
import project.project1.goal.certification.external.ExternalCertificationMethod;
import project.project1.goal.certification.external.ProblemGoalType;

import java.time.LocalDate;
import java.util.List;

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

    private List<Integer> targetProblems;

    // --- GitHub 전용 필드 ---
    private String targetRepository;
}
