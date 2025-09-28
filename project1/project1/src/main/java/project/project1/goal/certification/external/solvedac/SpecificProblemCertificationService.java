package project.project1.goal.certification.external;

import org.springframework.stereotype.Service;
import project.project1.goal.Goal;
import project.project1.user.SiteUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SpecificProblemCertificationService implements ExternalCertificationService{
    @Override
    public boolean certify(SiteUser user, Goal goal, String handle, List<SolvedSubmission> submissions) {
        if (goal.getProblemIds() == null) {
            throw new IllegalStateException("특정 문제 모드인데 problemIds가 없습니다.");
        }

        Set<Integer> targetProblems = Arrays.stream(goal.getProblemIds().split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toSet());

        LocalDateTime periodStart = calcPeriodStart(goal);

        return submissions.stream()
                .filter(s -> "AC".equals(s.getResult()))
                .filter(s -> s.getSubmittedAt().isAfter(periodStart))
                .anyMatch(s -> targetProblems.contains(s.getProblemId()));
    }

    private LocalDateTime calcPeriodStart(Goal goal) {
        // goal.cycle 기준으로 시작 시간 계산 (예: 하루, 주 단위)
        return LocalDate.now().atStartOfDay();
    }
}
