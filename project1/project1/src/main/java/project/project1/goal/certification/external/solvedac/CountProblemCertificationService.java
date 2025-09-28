package project.project1.goal.certification.external;

import org.springframework.stereotype.Service;
import project.project1.goal.Goal;
import project.project1.user.SiteUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CountProblemCertificationService implements ExternalCertificationService {
    @Override
    public boolean certify(SiteUser user, Goal goal, String handle, List<SolvedSubmission> submissions) {
        if (goal.getProblemCount() == null) {
            throw new IllegalStateException("갯수 모드인데 problemCount가 없습니다.");
        }

        LocalDateTime periodStart = calcPeriodStart(goal);

        long solvedCount = submissions.stream()
                .filter(s -> "AC".equals(s.getResult()))
                .filter(s -> s.getSubmittedAt().isAfter(periodStart))
                .map(SolvedSubmission::getProblemId)
                .distinct()
                .count();

        return solvedCount >= goal.getProblemCount();
    }

    private LocalDateTime calcPeriodStart(Goal goal) {
        // goal.cycle 기준으로 시작 시간 계산
        return LocalDate.now().atStartOfDay();
    }
}
