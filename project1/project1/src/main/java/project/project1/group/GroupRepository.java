package project.project1.group;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {

    Page<Group> findByNameContaining(String keyword, Pageable pageable);

    @Query("""
        SELECT g
        FROM Group g
        WHERE (:name IS NULL OR g.name LIKE %:name%)
        AND (:nickname IS NULL OR g.leader.nickname LIKE %:nickname%)
    """)
    Page<Group> searchByNameAndLeaderNickname(@Param("name") String name, @Param("nickname") String nickname, Pageable pageable);
}
