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
import project.project1.group.GroupRepository;
import project.project1.group.GroupService;
import project.project1.group.member.GroupMemberRepository;
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
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    /**
     * 새로운 목표를 JSON으로 받아 생성합니다.
     */
    @PostMapping
    public ResponseEntity<GoalResponseDto> createGoal(
            @RequestBody GoalCreateRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        log.info("===== Goal Create Request DTO 수신: {} =====", requestDto.toString());

        Long managerId = userDetails.getId();
        Group group = groupService.findById(requestDto.getGroupId()); // ◀◀ 'requestDto' 사용

        if (!group.getLeader().getId().equals(managerId)) {
            log.warn("목표 생성 권한 없음. GroupId: {}, UserId: {}", requestDto.getGroupId(), managerId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 서비스가 DTO를 받아 엔티티를 생성하고 저장
        Goal savedGoal = goalService.createGoal(requestDto, managerId, group);

        // 2. 엔티티를 DTO로 변환하여 반환합니다.
        GoalResponseDto responseDto = new GoalResponseDto(savedGoal);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * 특정 그룹의 목표 리스트를 JSON으로 반환합니다.
     * (기존 GoalController의 getGoalPage 메서드를 이동)
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getGoalPage(@PathVariable("groupId") Long groupId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long currentUserId = userDetails.getId();
        String currentUsername = userDetails.getUsername();

        boolean isMember = groupMemberRepository.existsByGroup_IdAndUser_Username(groupId, currentUsername);

        // 2. GroupRepository에서 Group을 직접 조회하여 리더인지 확인
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        boolean isLeader = group.getLeader() != null && group.getLeader().getId().equals(currentUserId);

        // 3. ◀ 멤버도 아니고 리더도 아니면 403 반환
        if (!isMember && !isLeader) {
            log.warn("목표 목록 조회 권한 없음. GroupId: {}, UserId: {}", groupId, currentUserId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("이 그룹의 목표를 볼 권한이 없습니다.");
        }

        List<Goal> goalEntities = goalService.getGoalsByGroupId(groupId);

        List<GoalListDto> goalDtos = goalEntities.stream()
                .map(goal -> GoalListDto.builder() // ◀ 빌더 시작
                        .id(goal.getId())
                        .name(goal.getName())
                        .endDate(goal.getEndDate())
                        .certificationType(goal.getCertificationType())
                        .externalMethod(goal.getExternalMethod())
                        .groupId(goal.getGroup() != null ? goal.getGroup().getId() : null)
                        .cycle(goal.getCycle())
                        .createdDate(goal.getCreatedDate())
                        .build() // ◀ 빌더 종료
                )
                .collect(Collectors.toList());

        return ResponseEntity.ok(new GoalPageResponse(goalDtos));
    }
}
