package project.project1.goal.certification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.project1.goal.Goal;
import project.project1.goal.GoalRepository;
import project.project1.goal.GoalServiceImpl;
import project.project1.goal.certification.certificationentity.CertificationManagerDto;
import project.project1.goal.certification.certificationentity.CertificationStatusDto;
import project.project1.goal.certification.certificationentity.CertificationUpdateDto;
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

@RestController
@RequestMapping("/api/certifications")
@RequiredArgsConstructor
public class CertificationController {

    private final CertificationService certificationService;
    private final GoalServiceImpl goalService;
    private final GroupService groupService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;

    /**
     * [그룹 멤버용] 인증 상태 '요약' 목록
     */
    @GetMapping("/goal/{goalId}/status")
    public ResponseEntity<List<CertificationStatusDto>> getCertificationStatuses(
            @PathVariable("goalId") Long goalId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = userDetails.getId();

        List<CertificationStatusDto> statuses = certificationService.getCertificationStatusesForGoal(goalId, currentUserId);
        return ResponseEntity.ok(statuses);
    }

    /**
     * [그룹 매니저용] 인증 '상세' 내용 조회
     */
    @GetMapping("/goal/{goalId}/manager/{certificationId}/status")
    public ResponseEntity<CertificationManagerDto> getCertificationManager(
            @PathVariable("goalId") Long goalId,
            @PathVariable("certificationId") Long certificationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = userDetails.getId();

        CertificationManagerDto certDetail = certificationService.getCertificationManager(goalId, certificationId, currentUserId);
        return ResponseEntity.ok(certDetail);
    }

    /**
     * [그룹 매니저용] 인증 상태 '수정' (승인/반려)
     */
    @PatchMapping("/goal/{goalId}/manager/{certificationId}/status")
    public ResponseEntity<CertificationManagerDto> updateCertificationStatus(
            @PathVariable("goalId") Long goalId,
            @PathVariable("certificationId") Long certificationId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CertificationUpdateDto updateDto
    ) {
        Long currentUserId = userDetails.getId();

        CertificationManagerDto updatedCertDto = certificationService.updateCertificationStatus(goalId, certificationId, currentUserId, updateDto);
        return ResponseEntity.ok(updatedCertDto);
    }

    @PostMapping("/text")
    public ResponseEntity<CertificationManagerDto> uploadText(
            @RequestBody TextCertificationRequest req,
            @RequestParam("goalId") Long goalId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();

        CertificationManagerDto certDto = certificationService.saveTextCertification(req, userId, goalId);
        return ResponseEntity.ok(certDto);
    }

    @PostMapping("/image")
    public ResponseEntity<CertificationManagerDto> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("goalId") Long goalId,
            @RequestParam("groupId") Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();

        CertificationManagerDto certDto = certificationService.saveImageCertification(file, userId, goalId, groupId);
        return ResponseEntity.ok(certDto);
    }

    @PostMapping("/video")
    public ResponseEntity<CertificationManagerDto> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("goalId") Long goalId,
            @RequestParam("groupId") Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();

        CertificationManagerDto certDto = certificationService.saveVideoCertification(file, userId, goalId, groupId);
        return ResponseEntity.ok(certDto);
    }

    @PostMapping("/external")
    public ResponseEntity<Certification> certifyExternal(
            @RequestBody ExternalCertificationRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = userDetails.getId();

        certificationService.certifyExternal(currentUserId, req.getGoalId(), req.getMethod());
        return ResponseEntity.ok().build();
    }

}
