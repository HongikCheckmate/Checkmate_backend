package project.project1.goal.certification.storage;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import project.project1.goal.Goal;
import project.project1.goal.certification.CertificationType;

@Entity
@DiscriminatorValue("VIDEO")
public class VideoGoal extends Goal {
    public VideoGoal() {
        super(CertificationType.VIDEO);
    }
}
