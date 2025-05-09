package project.project1.group;

import org.springframework.data.jpa.repository.JpaRepository;
import project.project1.user.SiteUser;

import java.util.Optional;


public interface GroupRepository extends JpaRepository<Group, Long> {
}
