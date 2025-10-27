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

        SolvedAcUser solvedAcUser = user.getSolvedAcUser();
        if (solvedAcUser == null) {
            return false;
        }
        String handle = solvedAcUser.getHandle();

        if (!(goal instanceof SolvedAcGoal)) {
            // Solved.ac 목표가 아니면 이 서비스에서 검증할 수 없습니다.
            // false를 반환하거나 예외를 던질 수 있습니다.
            throw new IllegalArgumentException("이 목표는 Solved.ac 목표가 아닙니다.");
        }

        // 3. 자식 타입으로 안전하게 형변환합니다.
        SolvedAcGoal solvedAcGoal = (SolvedAcGoal) goal;

        // 4. 형변환된 'solvedacGoal' 객체의 메서드를 사용합니다.
        return switch (solvedAcGoal.getProblemGoalType()) {

            case COUNT -> {
                // 'solvedAcGoal'의 헬퍼 메서드를 통해 startCount를 가져옵니다.
                int startCount = solvedAcGoal.getStartCountForUser(userId);
                yield solvedAcCertificationService.verifyNProblemsSolvedInPeriod(
                        handle,
                        solvedAcGoal.getProblemCount(), // solvedacGoal.get...
                        startCount
                );
            }

            case SPECIFIC -> {
                yield solvedAcCertificationService.verifySpecificProblemsSolved(
                        handle,
                        solvedAcGoal.getTargetProblemIds() // solvedacGoal.get...
                );
            }
        };
    }
}
