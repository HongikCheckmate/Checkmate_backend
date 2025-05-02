package project.project1.goal.certification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CertificationRepository extends JpaRepository<Certification, Long> {

    List<Certification> findByUserIdAndGoalId(Long userId, Long goalId);
    // 사용자가 해당 목표에 남긴 모든 인증 가져올때 사용
    boolean existsByUserIdAndGoalIdAndCertifiedAtBetween( //오늘 인증했는지 확인
            Long userId,
            Long goalId,
            LocalDateTime start,
            LocalDateTime end
    );
}
