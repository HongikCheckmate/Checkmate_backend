package project.project1.goal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import project.project1.goal.certification.CertificationType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@ResponseBody
@RequestMapping("/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService=goalService;
    }

    @PostMapping
    public Goal createGoal(@RequestBody Goal goal) {
        return goalService.createGoal(goal);
    }

    @GetMapping("/group/{groupId}")
    public GoalPageResponse getGoalPage(@PathVariable Long groupId) {
        List<Goal> goals = goalService.getGoalsByGroupId(groupId);
        List<String> certificationTypes = Arrays.stream(CertificationType.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        return new GoalPageResponse(goals, certificationTypes);
    }
}
