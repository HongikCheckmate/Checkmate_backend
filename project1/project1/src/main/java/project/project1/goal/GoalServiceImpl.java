package project.project1.goal;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.project1.goal.certification.external.ProblemGoalType;
import project.project1.goal.certification.external.github.GithubGoal;
import project.project1.goal.certification.external.solvedac.SolvedAcGoal;
import project.project1.goal.certification.external.solvedac.TargetProblem;
import project.project1.goal.certification.storage.ImageGoal;
import project.project1.goal.certification.storage.TextGoal;
import project.project1.goal.certification.storage.VideoGoal;
import project.project1.group.Group;
import project.project1.group.GroupRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final GroupRepository groupRepository;

    @Transactional
    @Override
    public Goal createGoal(GoalCreateRequestDto dto, Long managerId, Group group) {
        Goal goal;

        switch (dto.getCertificationType()) {
            case TEXT:
                goal = new TextGoal();
                break;
            case IMAGE:
                goal = new ImageGoal();
                break;
            case VIDEO:
                goal = new VideoGoal();
                break;
            case EXTERNAL:
                switch (dto.getExternalMethod()) {
                    case SOLVED_AC:
                        SolvedAcGoal solvedAcGoal = new SolvedAcGoal();
                        solvedAcGoal.setProblemGoalType(dto.getProblemGoalType());
                        if (dto.getProblemGoalType() == ProblemGoalType.COUNT) {
                            solvedAcGoal.setProblemCount(dto.getProblemCount());

                        } else if (dto.getProblemGoalType() == ProblemGoalType.SPECIFIC) {

                            // DTO에서 List<Integer>를 가져옵니다. (예: [1000, 1001])
                            List<Integer> problemIdList = dto.getTargetProblems();

                            if (problemIdList != null && !problemIdList.isEmpty()) {
                                // List<Integer>를 List<TargetProblem> 엔티티로 변환합니다.
                                List<TargetProblem> targetProblemEntities = problemIdList.stream()
                                        .map(problemId -> { // 1000, 1001...
                                            TargetProblem tp = new TargetProblem();
                                            tp.setProblemId(problemId);
                                            tp.setGoal(solvedAcGoal); // ◀ 부모-자식 연관관계 설정
                                            return tp;
                                        })
                                        .collect(Collectors.toList());

                                // SolvedAcGoal의 리스트에 추가 (CascadeType.ALL로 함께 저장됨)
                                solvedAcGoal.getTargetProblems().addAll(targetProblemEntities);
                            }
                        }
                        goal = solvedAcGoal;
                        break;
                    case GITHUB:
                        GithubGoal githubGoal = new GithubGoal();
                        githubGoal.setTargetRepository(dto.getTargetRepository());
                        goal = githubGoal;
                        break;
                    default:
                        throw new IllegalArgumentException("알 수 없는 외부 인증 방식입니다.");
                }
                break;
            default:
                throw new IllegalArgumentException("알 수 없는 인증 타입입니다.");
        }

        goal.setName(dto.getName());
        goal.setCycle(dto.getCycle());
        goal.setEndDate(dto.getEndDate());
        goal.setManagerId(managerId); // ◀ 파라미터로 받은 managerId
        goal.setGroup(group);         // ◀ 파라미터로 받은 group

        // ◀◀ 3. (추가) 엔티티를 DB에 저장
        return goalRepository.save(goal);
    }

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
