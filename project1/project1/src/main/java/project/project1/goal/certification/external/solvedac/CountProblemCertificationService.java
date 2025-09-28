package project.project1.goal.certification.external.solvedac;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.project1.goal.Goal;
import project.project1.goal.certification.external.ExternalCertificationService;
import project.project1.user.SiteUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CountProblemCertificationService implements ExternalCertificationService {
    private final SolvedAcClient solvedAcClient;

    @Override
    public boolean verify(String handle, int requiredCount) {
        int solvedCount = solvedAcClient.getSolvedCount(handle);
        return solvedCount >= requiredCount;
    }
}
