package project.project1.goal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import project.project1.goal.certification.CertificationType;
import project.project1.goal.certification.external.ProblemGoalType;
import project.project1.goal.certification.external.github.GithubGoal;
import project.project1.goal.certification.external.solvedac.SolvedAcGoal;
import project.project1.goal.certification.external.solvedac.TargetProblem;
import project.project1.goal.certification.storage.ImageGoal;
import project.project1.goal.certification.storage.TextGoal;
import project.project1.goal.certification.storage.VideoGoal;
import project.project1.group.Group;
import project.project1.group.GroupService;
import project.project1.user.CustomUserDetails;
import project.project1.user.UserService;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/goals") // API 엔드포인트는 /api/ prefix 사용
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;
    private final GroupService groupService;

    /**
     * 새로운 목표를 JSON으로 받아 생성합니다.
     */
    @PostMapping
    public ResponseEntity<Goal> createGoal(@Valid @RequestBody GoalCreateRequestDto dto,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("===== Goal Create Request DTO 수신: {} =====", dto.toString());
        Long managerId = userDetails.getId();
        Group group = groupService.findById(dto.getGroupId());

        // (기존 GoalController의 팩토리 로직을 그대로 가져옴)
        Goal newGoal = createGoalEntityFromDto(dto, managerId, group);

        Goal savedGoal = goalService.createGoal(newGoal);

        // 201 Created 응답과 함께 생성된 Goal 객체를 JSON으로 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(savedGoal);
    }

    /**
     * 특정 그룹의 목표 리스트를 JSON으로 반환합니다.
     * (기존 GoalController의 getGoalPage 메서드를 이동)
     */
    @GetMapping("/group/{groupId}")
    public GoalPageResponse getGoalPage(@PathVariable Long groupId) {
        List<Goal> goals = goalService.getGoalsByGroupId(groupId);
        List<String> certificationTypes = Arrays.stream(CertificationType.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        return new GoalPageResponse(goals, certificationTypes);
    }

    private Goal createGoalEntityFromDto(GoalCreateRequestDto dto, Long managerId, Group group) {

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
                            solvedAcGoal.setTargetProblems(parseTargetProblems(dto.getTargetProblemIds(), solvedAcGoal));
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
        goal.setManagerId(managerId);
        goal.setGroup(group);

        return goal;
    }

    private List<TargetProblem> parseTargetProblems(String problemIds, SolvedAcGoal goal) {
        // 1. null 이거나 "  " 같은 공백만 있는 경우 빈 리스트 반환
        if (problemIds == null || problemIds.isBlank()) {
            return new ArrayList<>();
        }

        List<TargetProblem> problems = new ArrayList<>();
        String[] ids = problemIds.split(","); // 예: "1001,,1002" -> ["1001", "", "1002"]

        for (String idStr : ids) {
            String trimmedIdStr = idStr.trim(); // 2. 앞뒤 공백 제거

            // 3. "1001"은 통과, "" (빈 문자열)은 무시
            if (trimmedIdStr.isEmpty()) {
                continue;
            }

            try {
                int problemId = Integer.parseInt(trimmedIdStr);

                // try 블록 안에서 객체 생성 및 리스트 추가
                TargetProblem tp = new TargetProblem();
                tp.setProblemId(problemId);
                tp.setGoal(goal);
                problems.add(tp); // 성공 시에만 추가

            } catch (NumberFormatException e) {
                // "abc" 처럼 숫자가 아닌 값은 무시
                System.err.println("잘못된 문제 ID 형식 (무시됨): " + idStr);
            }
        }
        return problems;
    }
}
