package project.project1.goal.certification.external.solvedac;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import project.project1.goal.Goal;
import project.project1.goal.certification.CertificationType;
import project.project1.goal.certification.external.ExternalCertificationMethod;
import project.project1.goal.certification.external.ProblemGoalType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@DiscriminatorValue("SOLVED_AC")
public class SolvedAcGoal extends Goal {
    @Enumerated(EnumType.STRING)
    private ProblemGoalType problemGoalType; // SPECIFIC or COUNT

    private Integer problemCount;    // COUNT 모드일 때

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TargetProblem> targetProblems = new ArrayList<>(); //SPECIFIC 모드

    // --- 생성자 ---
    // JPA가 객체 생성 시 + 우리가 new SolvedacGoal() 할 때 호출됨
    public SolvedAcGoal() {
        // 부모 생성자를 호출해 비즈니스 Enum 값을 설정
        super(CertificationType.EXTERNAL, ExternalCertificationMethod.SOLVED_AC);
    }

    // --- Solved.ac 전용 메서드 ---
    public List<Integer> getTargetProblemIds() {
        return targetProblems.stream()
                .map(TargetProblem::getProblemId)
                .collect(Collectors.toList());
    }

    public int getStartCountForUser(Long userId) {
        return this.getMembers().stream() // 부모(Goal)의 getMembers() 호출
                .filter(member -> member.getUser().getId().equals(userId))
                .findFirst()
                .map(member -> {
                    // 자식 타입(SolvedacGoalMember)으로 캐스팅
                    if (member instanceof SolvedAcGoalMember) {
                        return ((SolvedAcGoalMember) member).getStartCount();
                    }
                    throw new IllegalStateException("잘못된 GoalMember 타입입니다.");
                })
                .orElseThrow(() -> new IllegalStateException("해당 유저는 목표(" + this.getName() + ")에 참여하고 있지 않습니다."));
    }
}
