package project.project1.goal.certification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.project1.goal.Goal;
import project.project1.goal.GoalRepository;
import project.project1.goal.GoalServiceImpl;
import project.project1.goal.certification.external.ExternalCertificationRequest;
import project.project1.group.Group;
import project.project1.group.GroupService;
import project.project1.user.CustomUserDetails;
import project.project1.user.SiteUser;
import project.project1.user.UserRepository;
import project.project1.user.UserService;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/api/certifications")
@RequiredArgsConstructor
public class CertificationController {

    private final CertificationService certificationService;
    private final GoalServiceImpl goalService;
    private final GroupService groupService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;

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
    public ResponseEntity<Certification> uploadText(
            @RequestBody TextCertificationRequest req,
            @RequestParam("goalId") Long goalId,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        Certification cert = certificationService.saveTextCertification(req, userId, goalId);
        return ResponseEntity.ok(cert);
    }

    @PostMapping("/image")
    public ResponseEntity<Certification> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("goalId") Long goalId,
            @RequestParam("groupId") Long groupId,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        Certification cert = certificationService.saveImageCertification(file, userId, goalId, groupId);
        return ResponseEntity.ok(cert);
    }

    @PostMapping("/video")
    public ResponseEntity<Certification> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("goalId") Long goalId,
            @RequestParam("groupId") Long groupId,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        Certification cert = certificationService.saveVideoCertification(file, userId, goalId, groupId);
        return ResponseEntity.ok(cert);
    }

    @PostMapping("/external")
    public ResponseEntity<Certification> certifyExternal(
            @RequestBody ExternalCertificationRequest req,
            Principal principal
            ) {
        SiteUser currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        certificationService.certifyExternal(currentUser.getId(), req.getGoalId(), req.getMethod());
        return ResponseEntity.ok().build();
    }

    /**
     * 특정 목표에 대한 유저의 인증 내역 조회
     */
    @GetMapping("/{goalId}/user/{userId}")
    public ResponseEntity<List<Certification>> getUserCertifications(
            @PathVariable Long goalId,
            @PathVariable Long userId
    ) {
        List<Certification> certs = certificationService.getUserCertifications(goalId, userId);
        return ResponseEntity.ok(certs);
    }

}
