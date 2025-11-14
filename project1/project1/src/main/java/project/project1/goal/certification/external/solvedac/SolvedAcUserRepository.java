package project.project1.goal.certification.external.solvedac;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolvedAcUserRepository extends JpaRepository<SolvedAcUser, Long> {
}
