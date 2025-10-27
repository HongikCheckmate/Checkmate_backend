package project.project1.goal.certification.external.solvedac;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.project1.goal.Goal;
import project.project1.goal.GoalMember;
import project.project1.user.SiteUser;

@Entity
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("SOLVED_AC")
public class SolvedAcGoalMember extends GoalMember {
    @Column(nullable = false)
    private int startCount;
}
