package project.project1.goal.certification.storage;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import project.project1.goal.Goal;
import project.project1.goal.certification.CertificationType;

@Entity
@DiscriminatorValue("IMAGE")
public class ImageGoal extends Goal {
    public ImageGoal() {
        super(CertificationType.IMAGE);
    }
}
