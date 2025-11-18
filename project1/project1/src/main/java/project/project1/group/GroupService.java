package project.project1.group;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.project1.api.dto.GroupSummaryDto;
import project.project1.exception.ForbiddenException;
import project.project1.group.dto.GroupCreateRequestDto;
import project.project1.group.dto.GroupUpdateRequestDto;
import project.project1.group.member.GroupMember;
import project.project1.group.member.GroupMemberRepository;
import project.project1.user.SiteUser;
import project.project1.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public boolean isLeader(Long groupId, String username) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));
        return group.getLeader() != null && group.getLeader().getUsername().equals(username);
    }

    @Transactional
    public GroupSummaryDto updateGroup(Long groupId, String actorUsername, GroupUpdateRequestDto req) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));


        if (group.getLeader() == null || !group.getLeader().getUsername().equals(actorUsername)) {
            throw new ForbiddenException("그룹장만 수정할 수 있습니다.");
        }


        // 1) 이름 변경
        if (req.name() != null) {
            String name = req.name().trim();
            if (name.isEmpty()) throw new IllegalArgumentException("그룹명은 비어있을 수 없습니다.");
            group.setName(name);
        }


        // 2) 설명 변경
        if (req.description() != null) {
            group.setDescription(req.description().trim());
        }


        // 3) 리더 위임
        if (req.leader() != null) {
            Optional<SiteUser> target = userRepository.findByUsername(req.name());

            if(target.isEmpty()) {
                throw new IllegalArgumentException("해당 그룹원은 없는 유저네임입니다.");
            }

            if (!groupMemberRepository.existsByGroup_IdAndUser_Username(groupId, req.name())) {
                throw new IllegalArgumentException("리더는 그룹원에게만 위임할 수 있습니다.");
            }
            group.setLeader(target.get());
        }


        Group saved = groupRepository.save(group);
        return new GroupSummaryDto(
                saved.getId(), saved.getLeader().getUsername(), saved.getLeader().getNickname(), saved.getName(), saved.getDescription(), group.getGroupMembers().size()
        );
    }

    public Group createGroup(GroupCreateRequestDto dto){
        SiteUser leader = userRepository.findByUsername(dto.getRoomManager())
                .orElseThrow(() -> new NoSuchElementException("해당 리더는 존재하지 않는 ID 입니다."));

        String name = dto.getName();
        String description = dto.getDescription();
        boolean hidden = dto.isHidden();
        String password = dto.getPassword();

        Group.GroupBuilder groupBuilder = Group.builder();
        groupBuilder.name(name);
        groupBuilder.description(description);
        groupBuilder.leader(leader);

        if(password == null || password.isEmpty()){
            groupBuilder.password(null);
        } else {
            groupBuilder.password(passwordEncoder.encode(password));
        }

        groupBuilder.hidden(hidden);

        Group g = groupBuilder.build();
        g.addMember(leader);

        return groupRepository.save(g);
    }

    public void addUserToGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹 없음"));

        SiteUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        // 이미 멤버인지 체크
        boolean alreadyExists = groupMemberRepository.existsByGroup_IdAndUser_Id(groupId, userId);

        if (alreadyExists) {
            throw new IllegalStateException("이미 그룹 가입된 유저");
        }

        group.addMember(user);

        groupRepository.save(group);
    }

    public Group findById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));
    }

    @Transactional(readOnly = true)
    public Page<Group> searchGroupsByName(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) keyword = "";
        return groupRepository.findByNameContaining(keyword, pageable);
    }
}
