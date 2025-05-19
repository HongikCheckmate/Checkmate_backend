package project.project1.goal.certification;

import org.springframework.data.jpa.repository.JpaRepository;
import project.project1.goal.Goal;
import project.project1.user.SiteUser;

import java.time.LocalDateTime;
import java.util.List;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByGoal(Goal goal);
    List<Certification> findByUser(SiteUser user);
    List<Certification> findByUserAndGoalId(SiteUser user, Long goalId);

}
