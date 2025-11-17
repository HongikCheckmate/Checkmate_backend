package project.project1.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

import java.util.Optional;


@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupManageAPIController {

    private final GroupRepository groupRepository;
    private final GroupService groupService;
    private final GroupMemberService groupMemberService;

    @GetMapping("/{groupId}")
    public ResponseEntity<?> info(
            @PathVariable Long groupId
    ){

        Optional<Group> groupOptional = groupRepository.findById(groupId);

        if(groupOptional.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Group g = groupOptional.get();

        GroupSummaryDto groupSummaryDto = new GroupSummaryDto(
                g.getId(), g.getLeader().getUsername(), g.getLeader().getNickname(), g.getName(), g.getDescription(), g.getMember().size()
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


    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<?> handleForbidden(ForbiddenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
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
}