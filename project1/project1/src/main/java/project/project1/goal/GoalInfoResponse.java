package project.project1.goal;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class GoalInfoResponse {
    private Long goalId;
    private String goalName;
    private String cycle;
    private LocalDate endDate;
    private Long groupId;
    private String groupName;
}
