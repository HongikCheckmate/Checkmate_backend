package project.project1.goal;

import java.util.List;

public interface GoalService {
    Goal createGoal(GoalCreateRequestDto requestDto);
    List<Goal> getGoalsByGroupId(Long groupId);
    Goal findById(Long goalId);
}
