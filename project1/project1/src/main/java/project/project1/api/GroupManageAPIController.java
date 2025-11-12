package project.project1.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import project.project1.group.dto.MembersResponseDto;
import project.project1.group.member.GroupMemberService;


@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupManageAPIController {
    private final GroupMemberService service;

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
        if (!service.isMember(groupId, principal.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        MembersResponseDto body = service.listMembers(groupId, query, sort, reverse, page);
        return ResponseEntity.ok(body);
    }
}