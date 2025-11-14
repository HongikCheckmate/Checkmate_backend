package project.project1.goal;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.project1.goal.certification.CertificationType;
import project.project1.goal.certification.external.ExternalCertificationMethod;
import project.project1.goal.certification.external.ProblemGoalType;
import project.project1.goal.certification.external.solvedac.SolvedAcGoal;
import project.project1.goal.certification.external.solvedac.TargetProblem;
import project.project1.group.Group;
import project.project1.group.GroupRepository;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // ◀◀ final 필드에 대한 생성자를 자동으로 만듭니다.
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final GroupRepository groupRepository;

    @Transactional
    @Override
    public Goal createGoal(GoalCreateRequestDto requestDto) {

        // 1. 부모가 될 그룹(Group) 엔티티를 조회합니다.
        // (GroupRepository가 필요합니다. 없다면 주입받으세요.)
        Group group = groupRepository.findById(requestDto.getGroupId())
                .orElseThrow(() -> new NoSuchElementException("그룹을 찾을 수 없습니다: id=" + requestDto.getGroupId()));

        Goal goal; // 저장할 부모 타입 변수

        // 2. DTO의 인증 방식에 따라 적절한 엔티티를 생성합니다.
        if (requestDto.getCertificationType() == CertificationType.EXTERNAL &&
                requestDto.getExternalMethod() == ExternalCertificationMethod.SOLVED_AC) {

            // 2-1. SolvedAcGoal 생성
            SolvedAcGoal solvedAcGoal = new SolvedAcGoal();

            // Solved.ac 전용 데이터 매핑
            solvedAcGoal.setProblemGoalType(requestDto.getProblemGoalType());

            if (requestDto.getProblemGoalType() == ProblemGoalType.COUNT) {
                solvedAcGoal.setProblemCount(requestDto.getProblemCount());
            }
            else if (requestDto.getProblemGoalType() == ProblemGoalType.SPECIFIC) {
                // "1000,1001" 문자열을 -> List<TargetProblem> 엔티티로 변환
                if (requestDto.getTargetProblemIds() != null && !requestDto.getTargetProblemIds().isBlank()) {
                    List<TargetProblem> targetProblems =
                            Arrays.stream(requestDto.getTargetProblemIds().split(",")) // ["1000", "1001"]
                                    .map(String::trim) // " 1000" -> "1000"
                                    .map(Integer::parseInt) // "1000" -> 1000
                                    .map(problemId -> {
                                        // TargetProblem 엔티티 생성
                                        TargetProblem tp = new TargetProblem();
                                        tp.setProblemId(problemId);
                                        tp.setGoal(solvedAcGoal); // ◀◀ 부모-자식 연관관계 설정
                                        return tp;
                                    })
                                    .collect(Collectors.toList());

                    // ◀◀ SolvedAcGoal에 TargetProblem 리스트 설정
                    // (CascadeType.ALL 덕분에 부모만 저장해도 자식이 저장됨)
                    solvedAcGoal.getTargetProblems().addAll(targetProblems);
                }
            }
            goal = solvedAcGoal; // 부모 타입으로 할당
        }
        // (필요하다면 else if 로 GitHubGoal 등 다른 목표 타입 처리)
        else {
            // (예: 일반 Goal 또는 지원하지 않는 타입 예외 처리)
            throw new IllegalArgumentException("지원되지 않는 목표 유형입니다.");
        }

        // 3. 공통 필드(이름, 주기, 종료일, 그룹)를 매핑합니다.
        goal.setName(requestDto.getName());
        goal.setCycle(requestDto.getCycle());
        goal.setEndDate(requestDto.getEndDate());
        goal.setGroup(group); // ◀◀ 1번에서 찾은 그룹 엔티티 설정

        // 4. 엔티티를 저장합니다.
        return goalRepository.save(goal);}

    @Override
    public List<Goal> getGoalsByGroupId(Long groupId) {
        return goalRepository.findByGroupId(groupId);
    }

    @Override
    public Goal findById(Long goalId) {
        return goalRepository.findById(goalId)
                .orElseThrow(() -> new NoSuchElementException("해당 목표를 찾을 수 없습니다: id=" + goalId));
    }
}
