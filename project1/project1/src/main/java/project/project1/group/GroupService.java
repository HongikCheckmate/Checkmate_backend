package project.project1.group;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.project1.group.dto.GroupCreateRequestDto;
import project.project1.user.SiteUser;
import project.project1.user.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

        if(password.isEmpty() || password.isBlank()){
            groupBuilder.password(null);
        } else {
            groupBuilder.password(passwordEncoder.encode(password));
        }

        groupBuilder.hidden(hidden);

        Group g = groupBuilder.build();

        return groupRepository.save(g);

    }

    @Deprecated(forRemoval = true)
    // 그룹 생성 (방장(Member)의 ID가 반드시 존재해야 하며, 방장은 그룹 회원 목록에 자동 추가됨)
    public Group createGroup(String name, String description, String password, Long leaderId) {
        SiteUser leader = userRepository.findById(leaderId)
                .orElseThrow(() -> new NoSuchElementException("리더 회원이 존재하지 않습니다. ID=" + leaderId));
        Group group = Group.builder()
                .name(name)
                .password(password)
                .leader(leader)
                .build();
        return groupRepository.save(group);
    }

    // 그룹 가입 (회원이 그룹에 가입)
    public void joinGroup(Long groupId, Long memberId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("그룹이 존재하지 않습니다. ID=" + groupId));
        SiteUser member = userRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다. ID=" + memberId));
        group.addMember(member);
        groupRepository.save(group);
    }

    @Transactional(readOnly = true)
    public List<Group> findAllGroups() {
        return groupRepository.findAll();
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
