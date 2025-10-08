package project.project1.goal.certification.external.solvedac;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import project.project1.goal.Goal;

@Entity
@Getter
@Setter
@Builder
public class TargetProblem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int problemId; // 목표 문제 번호 하나

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private Goal goal;
}
