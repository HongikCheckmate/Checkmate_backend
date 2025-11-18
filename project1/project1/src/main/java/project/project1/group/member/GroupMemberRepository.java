package project.project1.group.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    boolean existsByGroup_IdAndUser_Username(Long groupId, String username);
    boolean existsByGroup_IdAndUser_Id(Long groupId, Long userId);
    Page<GroupMember> findByGroup_Id(Long groupId, Pageable pageable);
    Page<GroupMember> findByGroup_IdAndUser_NicknameContainingIgnoreCase(Long groupId, String nickname, Pageable pageable);
}
