package project.project1.goal.certification.storage;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import project.project1.goal.Goal;
import project.project1.goal.certification.CertificationType;

@Entity
@DiscriminatorValue("TEXT")
public class TextGoal extends Goal {
    public TextGoal() {
        super(CertificationType.TEXT);
    }
}
