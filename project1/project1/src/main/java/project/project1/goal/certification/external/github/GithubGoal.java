package project.project1.goal.certification.external.github;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.project1.goal.Goal;
import project.project1.goal.certification.CertificationType;
import project.project1.goal.certification.external.ExternalCertificationMethod;

@Entity
@Getter
@Setter
@DiscriminatorValue("GITHUB")
public class GithubGoal extends Goal {
    @Column(nullable = false)
    private String targetRepository; // GitHub 전용 필드

    // --- 생성자 ---
    public GithubGoal() {
        super(CertificationType.EXTERNAL, ExternalCertificationMethod.GITHUB);
    }
}
