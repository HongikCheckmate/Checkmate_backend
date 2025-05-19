package project.project1.goal;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import project.project1.goal.certification.CertificationType;
import project.project1.group.Group;
import project.project1.group.GroupService;
import project.project1.user.CustomUserDetails;
import project.project1.user.UserService;

import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/goals")
public class GoalController {

    private final GoalService goalService;
    private final UserService userService;
    private final GroupService groupService;

    public GoalController(GoalService goalService, UserService userService, GroupService groupService) {
        this.goalService = goalService;
        this.userService = userService;
        this.groupService = groupService;
    }

    // 목표설정 폼 HTML 화면 반환 (템플릿 엔진으로 렌더링)
    @GetMapping("/{groupId}/form")
    public String showGoalForm(@PathVariable("groupId") Long groupId,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) throws AccessDeniedException {

        Long currentUserId = userDetails.getId();
        Group group = groupService.findById(groupId);

        boolean isMember = group.getMember().stream().anyMatch(member -> member.getId().equals(currentUserId));
        boolean isManager = group.getManager().getId().equals(currentUserId);
        if (!isMember || !isManager) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        model.addAttribute("groupId", groupId);
        List<Group> groups = groupService.findAllGroups();
        model.addAttribute("certificationTypes", CertificationType.values());
        model.addAttribute("goal", new Goal()); // 폼 데이터를 담을 객체

        return "goal/goal-form";

    }

    //목표 저장
    @PostMapping("/submit")
    public String createGoal(@ModelAttribute Goal goal,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             @RequestParam(name = "groupId", required = false) Long groupId) {

        Long managerId = userDetails.getId();
        goal.setManagerId(managerId);

        System.out.println(">> Group ID received in request: " + groupId);
        if (groupId == null) {
                throw new IllegalStateException("Group ID is missing!");
        }
        //Group을 DB에서 조회, Goal객체에 설정
        Group group = groupService.findById(groupId);
        goal.setGroup(group);

        goalService.createGoal(goal);
        return "redirect:/goals/form?groupId=" + goal.getGroup().getId();
    }

    @GetMapping("/group/{groupId}")
    @ResponseBody
    public GoalPageResponse getGoalPage(@PathVariable Long groupId) {
        List<Goal> goals = goalService.getGoalsByGroupId(groupId);
        List<String> certificationTypes = Arrays.stream(CertificationType.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        return new GoalPageResponse(goals, certificationTypes);
    }
}
