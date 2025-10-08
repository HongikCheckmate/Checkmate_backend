package project.project1.goal.certification.external.solvedac;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.project1.goal.Goal;
import project.project1.goal.GoalRepository;
import project.project1.user.SiteUser;
import project.project1.user.UserRepository;

@Service
@RequiredArgsConstructor
public class StudyProgressService {
    private final SolvedAcCertificationService solvedAcCertificationService;
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;

    /**
     * 특정 사용자의 특정 목표에 대한 진행 상황을 검증하고 그 결과를 반환합니다.
     *
     * @param userId 검증할 사용자의 ID
     * @param goalId 검증할 목표의 ID
     * @return 목표 달성 여부 (true/false)
     */
    public boolean checkGoalProgress(Long userId, Long goalId) {
        SiteUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("목표를 찾을 수 없습니다."));

        String handle = user.getSolvedAcHandle();

        return switch (goal.getProblemGoalType()) {

            case COUNT -> {
                // 3. Goal 엔티티의 헬퍼 메서드를 통해 startCount를 가져옵니다.
                int startCount = goal.getStartCountForUser(userId);
                yield solvedAcCertificationService.verifyNProblemsSolvedInPeriod(
                        handle,
                        goal.getProblemCount(),
                        startCount
                );
            }

            case SPECIFIC -> {
                yield solvedAcCertificationService.verifySpecificProblemsSolved(
                        handle,
                        goal.getTargetProblemIds()
                );
            }
        };
    }
}
