package project.project1.goal.certification;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import project.project1.goal.Goal;
import project.project1.goal.certification.external.ExternalCertificationMethod;
import project.project1.group.Group;
import project.project1.user.SiteUser;

import java.time.LocalDateTime;

@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "certification_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@Setter
public abstract class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private SiteUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private Goal goal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertificationStatus status;

    @CreatedDate
    private LocalDateTime createdDate; //제출시간

    private LocalDateTime certifiedAt; //승인시간


}
