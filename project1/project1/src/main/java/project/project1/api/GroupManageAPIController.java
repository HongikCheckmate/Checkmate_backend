package project.project1.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import project.project1.api.dto.GroupSummaryDto;
import project.project1.exception.ForbiddenException;
import project.project1.group.Group;
import project.project1.group.GroupRepository;
import project.project1.group.GroupService;
import project.project1.group.dto.GroupUpdateRequestDto;
import project.project1.group.dto.MembersResponseDto;
import project.project1.group.member.GroupMemberService;
import project.project1.user.SiteUser;
import project.project1.user.UserRepository;
import project.project1.user.UserService;

import java.util.Optional;


@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupManageAPIController {

    private final GroupRepository groupRepository;
    private final GroupService groupService;
    private final GroupMemberService groupMemberService;
    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/{groupId}")
    public ResponseEntity<?> info(
            @PathVariable Long groupId
    ){

        Optional<Group> groupOptional = groupRepository.findById(groupId);

        if(groupOptional.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Group g = groupOptional.get();

        GroupSummaryDto groupSummaryDto = new GroupSummaryDto(
                g.getId(), g.getLeader().getUsername(), g.getLeader().getNickname(), g.getName(), g.getDescription(), g.getGroupMembers().size()
        );

        return new ResponseEntity<>(groupSummaryDto, HttpStatus.OK);
    }

    @PatchMapping("/{groupId}")
    public ResponseEntity<?> update(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long groupId,
            @RequestBody @Valid GroupUpdateRequestDto body
    ) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        GroupSummaryDto dto = groupService.updateGroup(groupId, principal.getUsername(), body);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> delete(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long groupId
    ){
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Group> group = groupRepository.findById(groupId);
        if(group.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Group g = group.get();
        if(!g.getLeader().getUsername().equals(principal.getUsername())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else if(g.getGroupMembers().size() > 1) {
            return handleBadRequest(new IllegalArgumentException("그룹장을 제외한 멤버가 없어야 그룹을 삭제할 수 있습니다."));
        } else{
            groupService.removeUserFromGroup(g.getId(), g.getLeader().getId());
            groupRepository.deleteById(groupId);
            return ResponseEntity.ok().build();
        }

    }


    @GetMapping("/{groupId}/members")
    public ResponseEntity<?> list(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long groupId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "false") boolean reverse,
            @RequestParam(required = false, defaultValue = "0") int page
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!groupMemberService.isMember(groupId, principal.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        MembersResponseDto body = groupMemberService.listMembers(groupId, query, sort, reverse, page);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{groupId}/members/{username}")
    public ResponseEntity<?> deleteMember(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long groupId,
            @PathVariable String username,
            @RequestParam(required = false) String reason
    ){
        Optional<Group> group = groupRepository.findById(groupId);
        if(group.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Group g = group.get();

        SiteUser user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저 찾기 실패"));

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isLeader = g.getLeader().getUsername().equals(principal.getUsername());

        if(username.equals(principal.getUsername())){
            if(isLeader){
                return handleBadRequest(new IllegalArgumentException("그룹장은 탈퇴할 수 없습니다. 그룹장을 위임하거나, 그룹 삭제 기능을 이용해주세요."));
            } else {
                groupService.removeUserFromGroup(user.getId(), g.getId());
            }

        } else {
            if(isLeader){
                groupService.removeUserFromGroup(user.getId(), g.getId());
            } else {
                return handleForbidden(new ForbiddenException("강제 퇴장은 그룹장만 가능합니다."));
            }
        }

        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<?> handleForbidden(ForbiddenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}