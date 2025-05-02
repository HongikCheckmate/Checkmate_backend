package project.project1.goal.certification;

import org.springframework.web.bind.annotation.*;
import project.project1.goal.external.GitHubCertificationService;

@RestController
@RequestMapping("/certifications/external")
public class ExternalCertificationController {
    private final GitHubCertificationService gitHubCertificationService;

    public ExternalCertificationController(GitHubCertificationService gitHubCertificationService) {
        this.gitHubCertificationService = gitHubCertificationService;
    }

    @GetMapping("/github/{username}")
    public boolean verifyGitHub(@PathVariable("username") String username) {
        return gitHubCertificationService.verifyCertification(username);
    }

}
