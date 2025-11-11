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
@DiscriminatorValue("VIDEO")
public class VideoCertification extends Certification {
    private String videoUrl;
}
