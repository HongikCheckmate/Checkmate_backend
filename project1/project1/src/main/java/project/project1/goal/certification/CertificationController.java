package project.project1.goal.certification;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.project1.goal.external.ExternalCertificationService;

@RestController
@RequestMapping("/certifications")
public class CertificationController {

    private final CertificationService certificationService;
    private final ExternalCertificationService githubService;

    public CertificationController(CertificationService cs, ExternalCertificationService gs) {
        this.certificationService = cs;
        this.githubService = gs;
    }

    @PostMapping("/text")
    public ResponseEntity<Void> uploadText(@RequestBody TextCertificationRequest req) {
        certificationService.saveTextCertification(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/image")
    public ResponseEntity<Void> uploadImage(@RequestParam("file") MultipartFile file,
                                            @RequestParam("userId") Long userId,
                                            @RequestParam("goalId") Long goalId) {
        certificationService.saveImageCertification(file, userId, goalId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/github/{username}")
    public ResponseEntity<Boolean> verifyGithub(@PathVariable String username) {
        boolean result = githubService.verifyCertification(username);
        return ResponseEntity.ok(result);
    }
}
