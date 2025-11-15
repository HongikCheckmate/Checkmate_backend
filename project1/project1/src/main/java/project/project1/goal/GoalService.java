package project.project1.goal;

import project.project1.group.Group;

import java.util.List;

public interface GoalService {
    Goal createGoal(GoalCreateRequestDto requestDto, Long managerId, Group group);
    List<Goal> getGoalsByGroupId(Long groupId);
    Goal findById(Long goalId);
}
