package project.project1.goal.certification.certificationentity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.project1.goal.certification.Certification;

@Entity
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("GITHUB")
public class GithubCertification extends Certification {
    private String commitSha; // 인증에 사용된 커밋의 SHA
}
