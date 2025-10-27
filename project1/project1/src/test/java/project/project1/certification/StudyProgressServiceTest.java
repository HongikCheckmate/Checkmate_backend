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
import project.project1.goal.certification.external.solvedac.*;
import project.project1.goal.certification.storage.TextGoal;
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
    private GoalRepository goalRepository; // GoalRepository<Goal> (부모 타입)

    // --- (수정) fakeGoal의 타입을 Goal -> SolvedacGoal로 변경 ---
    private SiteUser fakeUser;
    private SolvedAcGoal fakeGoal; // 👈 (수정)
    private SolvedAcUser fakeSolvedAcUser;
    private Long userId = 1L;
    private Long goalId = 1L;

    @BeforeEach
    void setUp() {
        String handle = "testuser_handle";

        fakeUser = SiteUser.builder()
                .id(userId)
                .username("testuser")
                .build();

        fakeSolvedAcUser = SolvedAcUser.builder()
                .siteUser(fakeUser)
                .handle(handle)
                .build();

        fakeUser.setSolvedAcUser(fakeSolvedAcUser);

        // --- (수정) Goal.builder() -> new SolvedacGoal() ---
        // 'Goal'은 추상 클래스이므로 'new Goal()'이나 'Goal.builder()' 사용 불가
        // 실제 자식 클래스인 'SolvedacGoal'을 생성해야 함
        fakeGoal = new SolvedAcGoal(); // 👈 (수정)
        fakeGoal.setId(goalId);
        fakeGoal.setName("Test Goal");
        // ----------------------------------------------------

        given(userRepository.findById(userId)).willReturn(Optional.of(fakeUser));

        // --- (수정) goalRepository가 자식 타입인 fakeGoal을 반환하도록 설정 ---
        // StudyProgressService는 Goal 타입으로 받지만,
        // (goal instanceof SolvedacGoal) 체크가 통과됨
        given(goalRepository.findById(goalId)).willReturn(Optional.of(fakeGoal)); // 👈 (수정)
    }

    @Test
    @DisplayName("COUNT 목표: 10 문제 이상 풀기 목표를 성공적으로 달성한다")
    void checkGoalProgress_Count_Success() {
        // given (준비)
        int startCount = 100;
        int requiredCount = 10;
        String handle = fakeUser.getSolvedAcUser().getHandle();

        // fakeGoal은 이미 SolvedacGoal 타입이므로 바로 setter 사용
        fakeGoal.setProblemGoalType(ProblemGoalType.COUNT);
        fakeGoal.setProblemCount(requiredCount);

        // --- (수정) SolvedAcGoalMember.builder() -> new SolvedAcGoalMember() ---
        // (만약 @Builder를 자식 엔티티에도 추가했다면 .builder() 사용 가능)
        SolvedAcGoalMember fakeMember = new SolvedAcGoalMember(); // 👈 (수정)
        fakeMember.setUser(fakeUser);
        fakeMember.setGoal(fakeGoal);
        fakeMember.setStartCount(startCount);
        // ------------------------------------------------------------------

        fakeGoal.setMembers(List.of(fakeMember));

        // CertificationService의 동작 정의
        given(certificationService.verifyNProblemsSolvedInPeriod(handle, requiredCount, startCount))
                .willReturn(true);

        // when (실행)
        boolean result = studyProgressService.checkGoalProgress(userId, goalId);

        // then (검증)
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("SPECIFIC 목표: 특정 문제 풀기 목표를 성공적으로 달성한다")
    void checkGoalProgress_Specific_Success() {
        // given (준비)
        String handle = fakeUser.getSolvedAcUser().getHandle();
        fakeGoal.setProblemGoalType(ProblemGoalType.SPECIFIC);

        // --- (수정) TargetProblem.builder() -> new TargetProblem() ---
        // (만약 @Builder를 TargetProblem에 추가했다면 .builder() 사용 가능)
        TargetProblem problem1 = new TargetProblem(); // 👈 (수정)
        problem1.setProblemId(1000);
        problem1.setGoal(fakeGoal); // 👈 (수정) fakeGoal은 SolvedacGoal 타입이므로 OK

        TargetProblem problem2 = new TargetProblem(); // 👈 (수정)
        problem2.setProblemId(1001);
        problem2.setGoal(fakeGoal); // 👈 (수정)
        // ---------------------------------------------------------

        fakeGoal.setTargetProblems(List.of(problem1, problem2));
        List<Integer> targetIds = List.of(1000, 1001);

        // CertificationService의 동작 정의
        given(certificationService.verifySpecificProblemsSolved(handle, targetIds))
                .willReturn(true);

        // when (실행)
        boolean result = studyProgressService.checkGoalProgress(userId, goalId);

        // then (검증)
        assertThat(result).isTrue();
    }

    // (추가 권장) Solved.ac 목표가 아닐 때 예외가 발생하는지 테스트
    @Test
    @DisplayName("목표 타입이 SolvedacGoal이 아니면 예외를 발생시킨다")
    void checkGoalProgress_NotSolvedacGoal_ThrowsException() {
        // given (준비)
        // TextGoal 등 다른 타입의 Goal을 생성
        Goal otherGoal = new TextGoal(); // 'TextGoal'은 Goal을 상속
        otherGoal.setId(goalId);

        // goalRepository가 SolvedacGoal이 아닌 'TextGoal'을 반환하도록 설정
        given(goalRepository.findById(goalId)).willReturn(Optional.of(otherGoal));

        // when & then (실행 및 검증)
        // StudyProgressService의 (goal instanceof SolvedacGoal) 체크에 걸려 예외 발생
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
                    studyProgressService.checkGoalProgress(userId, goalId);
                }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이 목표는 Solved.ac 목표가 아닙니다.");
    }
}
