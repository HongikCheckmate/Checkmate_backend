package project.project1.goal.certification.external.solvedac;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.project1.goal.Goal;
import project.project1.goal.certification.external.ExternalCertificationService;


@Service
@RequiredArgsConstructor
public class SpecificProblemCertificationService implements ExternalCertificationService {
    private final SolvedAcClient solvedAcClient;

    @Override
    public boolean verify(String handle, int problemId) {
        return solvedAcClient.hasSolvedProblem(handle, problemId);
    }
}
