package project.project1.goal;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import project.project1.goal.certification.CertificationType;
import project.project1.goal.certification.external.ProblemGoalType;
import project.project1.goal.certification.external.solvedac.GoalMember;
import project.project1.goal.certification.external.solvedac.TargetProblem;
import project.project1.group.Group;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; //목표이름

    @Column(nullable = false)
    private int cycle; //주기

    private LocalDate endDate; //끝나는 날짜

    @Enumerated(EnumType.STRING)
    private CertificationType certificationType; //인증방식

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TargetProblem> targetProblems = new ArrayList<>(); //SPECIFIC 모드

    public List<Integer> getTargetProblemIds() {
        return targetProblems.stream()
                .map(TargetProblem::getProblemId)
                .collect(Collectors.toList());
    }

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoalMember> members = new ArrayList<>();

    private Integer problemCount;    // COUNT 모드일 때

    public int getStartCountForUser(Long userId) {
        return this.getMembers().stream()
                .filter(member -> member.getUser().getId().equals(userId))
                .findFirst()
                .map(GoalMember::getStartCount)
                .orElseThrow(() -> new IllegalStateException("해당 유저는 목표(" + this.name + ")에 참여하고 있지 않습니다."));
    }

    @Enumerated(EnumType.STRING)
    private ProblemGoalType problemGoalType; // SPECIFIC or COUNT

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    private Long managerId; //방장아이디

    @CreatedDate
    private LocalDateTime createdDate; // 목표 생성일
}
