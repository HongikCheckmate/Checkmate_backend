package project.project1.goal;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GoalPageResponse { //응답 DTO
    private List<Goal> goals;
    private List<String> certificationTypes;
}
