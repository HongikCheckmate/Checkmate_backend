package project.project1.goal.certification;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.project1.goal.Goal;
import project.project1.goal.GoalServiceImpl;
import project.project1.goal.certification.external.ExternalCertificationService;
import project.project1.group.Group;
import project.project1.group.GroupService;
import project.project1.user.CustomUserDetails;
import project.project1.user.SiteUser;
import project.project1.user.UserService;

import java.nio.file.AccessDeniedException;

@Controller
@RequestMapping("/certifications")
public class CertificationController {

    private final CertificationService certificationService;
    private final ExternalCertificationService githubService;
    private final GoalServiceImpl goalService;
    private final GroupService groupService;
    private final UserService userService;

    public CertificationController(CertificationService certificationService,
                                   ExternalCertificationService githubService,
                                   GoalServiceImpl goalService,
                                   GroupService groupService,
                                   UserService userService) {
        this.certificationService = certificationService;
        this.githubService = githubService;
        this.goalService = goalService;
        this.groupService = groupService;
        this.userService = userService;
    }

    @GetMapping("/{groupId}/certification-info/{goalId}")
    public String certificationForm(@PathVariable("goalId") Long goalId,
                                    @PathVariable("groupId") Long groupId,
                                    Model model,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) throws AccessDeniedException {
        Goal goal = goalService.findById(goalId);
        Group group = groupService.findById(groupId);
        SiteUser user = userService.findById(userDetails.getId());

        Long currentUserId = userDetails.getId();
        Group groupmenber = groupService.findById(groupId);

        boolean isMember = groupmenber.getMember().stream().anyMatch(member -> member.getId().equals(currentUserId));
        if (!isMember) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        if (goal == null || group == null) {
            return "redirect:/error";
        }

        model.addAttribute("goal", goal);
        model.addAttribute("group", group);
        model.addAttribute("userId", userDetails.getId());
        model.addAttribute("certificationType", goal.getCertificationType());

        return "goal/certification-form";
    }

    @PostMapping("/text")
    public ResponseEntity<Void> uploadText(@ModelAttribute TextCertificationRequest req,
                                           @RequestParam("goalId") Long goalId,
                                           @AuthenticationPrincipal(expression = "id") Long userId) {
        certificationService.saveTextCertification(req, userId, goalId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/image")
    public ResponseEntity<Void> uploadImage(@RequestParam("file") MultipartFile file,
                                            @AuthenticationPrincipal(expression = "id") Long userId,
                                            @RequestParam("goalId") Long goalId,
                                            @RequestParam("groupId") Long groupId) {
        certificationService.saveImageCertification(file, userId, goalId, groupId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/video")
    public ResponseEntity<Void> uploadVideo(@RequestParam("file") MultipartFile file,
                                            @AuthenticationPrincipal(expression = "id") Long userId,
                                            @RequestParam("goalId") Long goalId,
                                            @RequestParam("groupId") Long groupId) {
        certificationService.saveVideoCertification(file, userId, goalId, groupId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/github/{username}")
    public ResponseEntity<Boolean> verifyGithub(@PathVariable String username) {
        boolean result = githubService.verifyCertification(username);
        return ResponseEntity.ok(result);
    }
}
