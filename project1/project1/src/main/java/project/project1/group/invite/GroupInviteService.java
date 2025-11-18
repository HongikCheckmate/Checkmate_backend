package project.project1.group.invite;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.project1.group.Group;
import project.project1.group.GroupRepository;
import project.project1.group.GroupService;
import project.project1.group.invite.dto.InviteActionRequestDto;
import project.project1.group.invite.dto.InviteCreateRequestDto;
import project.project1.group.invite.dto.InviteResponseDto;
import project.project1.group.member.GroupMemberService;
import project.project1.user.SiteUser;
import project.project1.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupInviteService {
    private final GroupInviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberService groupMemberService;
    private final GroupService groupService;

    // 초대 생성
    public GroupInvite createInvite(InviteCreateRequestDto dto) {
        SiteUser inviter = userRepository.findById(dto.getInviterId())
                .orElseThrow(() -> new IllegalArgumentException("Inviter not found"));
        SiteUser invitee = userRepository.findById(dto.getInviteeId())
                .orElseThrow(() -> new IllegalArgumentException("Invitee not found"));
        Group group = groupRepository.findById(dto.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        GroupInvite invite = GroupInvite.builder()
                .inviter(inviter)
                .invitee(invitee)
                .group(group)
                .status(InviteStatus.PENDING)
                .build();

        return inviteRepository.save(invite);
    }

    // B(피초대자)가 자신의 초대 현황 확인
    public List<InviteResponseDto> getMyInvites(Long userId) {
        return inviteRepository.findByInviteeId(userId).stream()
                .map(invite -> InviteResponseDto.builder()
                        .id(invite.getId())
                        .inviterName(invite.getInviter().getUsername())
                        .invitedGroupId(invite.getGroup().getId())
                        .status(invite.getStatus())
                        .build())
                .toList();
    }

    // 초대 수락 or 거절
    public GroupInvite actOnInvite(InviteActionRequestDto dto) {
        GroupInvite invite = inviteRepository.findById(dto.getInviteId())
                .orElseThrow(() -> new IllegalArgumentException("Invite not found"));

        Group g = invite.getGroup();
        SiteUser invitee = invite.getInvitee();

        if(groupMemberService.isMember(g.getId(), invitee.getId())){
            throw new IllegalArgumentException("Already in group");
        }

        if ("ACCEPT".equalsIgnoreCase(dto.getAction())) {
            invite.setStatus(InviteStatus.ACCEPTED);
            groupService.addUserToGroup(g.getId(), invitee.getId());
            // TODO: 그룹 멤버십 로직 추가 가능 (ex. group.addMember(invitee))
        } else if ("REJECT".equalsIgnoreCase(dto.getAction())) {
            invite.setStatus(InviteStatus.REJECTED);
        }
        return inviteRepository.save(invite);
    }
}
