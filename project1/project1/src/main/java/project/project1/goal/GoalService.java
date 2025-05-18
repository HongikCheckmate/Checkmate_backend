package project.project1.goal;

import java.util.List;

public interface GoalService {
    Goal createGoal(Goal goal);
    List<Goal> getGoalsByGroupId(Long groupId);
    Goal findById(Long goalId);
}
