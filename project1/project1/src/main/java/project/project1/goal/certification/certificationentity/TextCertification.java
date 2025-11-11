package project.project1.goal.certification.certificationentity;

import jakarta.persistence.DiscriminatorValue;
import project.project1.goal.certification.Certification;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.project1.goal.certification.Certification;

@Entity
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("TEXT")
public class TextCertification extends Certification {
    private String TextUrl;
    private String content;
}
