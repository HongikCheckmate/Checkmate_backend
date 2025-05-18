package project.project1.goal.certification;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import project.project1.goal.Goal;
import project.project1.goal.GoalService;
import project.project1.group.Group;
import project.project1.group.GroupService;
import project.project1.user.CustomUserDetails;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/manager")
public class ManagerController {
    private final CertificationService certificationService;
    private final GoalService goalService;
    private final GroupService groupService;

    public ManagerController(CertificationService certificationService,
                             GoalService goalService,
                             GroupService groupService) {
        this.certificationService = certificationService;
        this.goalService = goalService;
        this.groupService = groupService;
    }

    @GetMapping("/certifications/{groupId}/{goalId}")
    public String viewCertifications(@PathVariable("groupId") Long groupId,
                                     @PathVariable("goalId") Long goalId,
                                     Model model,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {

        Goal goal = goalService.findById(goalId);
        Group group = groupService.findById(groupId);

        // 인증된 사용자가 방장이 아닐 경우 접근 차단
        if (!goal.getManagerId().equals(userDetails.getId())) {
            return "redirect:/access-denied"; // 혹은 오류 페이지
        }

        Map<Integer, List<Certification>> certifications = certificationService.getCertificationsGroupedByCycle(goal);

        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("group", group);
        model.addAttribute("goal", goal);
        model.addAttribute("certifications", certifications);

        return "goal/manager-certifications"; // 앞서 만든 HTML
    }
}
