package project.project1.certification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.project1.goal.Goal;
import project.project1.goal.GoalRepository;
import project.project1.goal.certification.external.ProblemGoalType;
import project.project1.goal.certification.external.solvedac.GoalMember;
import project.project1.goal.certification.external.solvedac.SolvedAcCertificationService;
import project.project1.goal.certification.external.solvedac.StudyProgressService;
import project.project1.goal.certification.external.solvedac.TargetProblem;
import project.project1.user.SiteUser;
import project.project1.user.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class StudyProgressServiceTest {
    @InjectMocks
    private StudyProgressService studyProgressService;

    @Mock
    private SolvedAcCertificationService certificationService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GoalRepository goalRepository;

    // 테스트 간에 공유될 가짜 객체들을 필드로 선언
    private SiteUser fakeUser;
    private Goal fakeGoal;
    private Long userId = 1L;
    private Long goalId = 1L;

    // @BeforeEach: 각 @Test 메서드가 실행되기 전에 항상 먼저 실행되는 부분
    @BeforeEach
    void setUp() {
        // 공통적인 User, Goal 객체 생성 및 Repository Mocking을 여기서 처리
        fakeUser = SiteUser.builder()
                .id(userId)
                .username("testuser")
                .solvedAcHandle("testuser_handle")
                .build();

        fakeGoal = Goal.builder()
                .id(goalId)
                .name("Test Goal")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(fakeUser));
        given(goalRepository.findById(goalId)).willReturn(Optional.of(fakeGoal));
    }

    @Test
    @DisplayName("COUNT 목표: 기간 내 문제 풀기 목표를 성공적으로 달성한다")
    void checkGoalProgress_Count_Success() {
        // given (준비) - COUNT 목표 시나리오에만 해당하는 부분
        int startCount = 100;
        int requiredCount = 10;

        fakeGoal.setProblemGoalType(ProblemGoalType.COUNT);
        fakeGoal.setProblemCount(requiredCount);

        // [핵심 수정] GoalMember 객체를 직접 만들어서 Goal에 설정 (상태 설정)
        GoalMember fakeMember = GoalMember.builder()
                .user(fakeUser)
                .goal(fakeGoal)
                .startCount(startCount)
                .build();
        fakeGoal.setMembers(List.of(fakeMember));

        // CertificationService의 동작 정의
        given(certificationService.verifyNProblemsSolvedInPeriod(fakeUser.getSolvedAcHandle(), requiredCount, startCount))
                .willReturn(true);

        // when (실행)
        boolean result = studyProgressService.checkGoalProgress(userId, goalId);

        // then (검증)
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("SPECIFIC 목표: 특정 문제 풀기 목표를 성공적으로 달성한다")
    void checkGoalProgress_Specific_Success() {
        // given (준비) - SPECIFIC 목표 시나리오에만 해당하는 부분
        fakeGoal.setProblemGoalType(ProblemGoalType.SPECIFIC);

        // 목표 문제 설정
        TargetProblem problem1 = TargetProblem.builder().problemId(1000).goal(fakeGoal).build();
        TargetProblem problem2 = TargetProblem.builder().problemId(1001).goal(fakeGoal).build();
        fakeGoal.setTargetProblems(List.of(problem1, problem2));

        List<Integer> targetIds = List.of(1000, 1001);

        // CertificationService의 동작 정의
        given(certificationService.verifySpecificProblemsSolved(fakeUser.getSolvedAcHandle(), targetIds))
                .willReturn(true);

        // when (실행)
        boolean result = studyProgressService.checkGoalProgress(userId, goalId);

        // then (검증)
        assertThat(result).isTrue();
    }
}
