package project.project1.goal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
public class GoalPageResponse { //응답 DTO
    private List<GoalListDto> goals;

    public GoalPageResponse(List<GoalListDto> goals) {
        this.goals = goals;
    }
}
