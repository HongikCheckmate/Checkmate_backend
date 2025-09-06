package project.project1.group.invite;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.project1.group.invite.dto.InviteActionRequestDto;
import project.project1.group.invite.dto.InviteCreateRequestDto;
import project.project1.group.invite.dto.InviteResponseDto;

import java.util.List;

@RestController
@RequestMapping("/api/invites")
@RequiredArgsConstructor
public class GroupInviteController {
    private final GroupInviteService inviteService;

    // A가 B를 그룹에 초대
    @PostMapping
    public ResponseEntity<GroupInvite> createInvite(@RequestBody InviteCreateRequestDto dto) {
        return ResponseEntity.ok(inviteService.createInvite(dto));
    }

    // B가 마이페이지에서 초대 현황 확인
    @GetMapping("/{userId}")
    public ResponseEntity<List<InviteResponseDto>> getMyInvites(@PathVariable Long userId) {
        return ResponseEntity.ok(inviteService.getMyInvites(userId));
    }

    // B가 초대 수락/거절
    @PostMapping("/action")
    public ResponseEntity<GroupInvite> actOnInvite(@RequestBody InviteActionRequestDto dto) {
        return ResponseEntity.ok(inviteService.actOnInvite(dto));
    }
}
