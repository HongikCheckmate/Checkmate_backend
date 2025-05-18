package project.project1.goal.certification;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import project.project1.goal.Goal;
import project.project1.group.Group;
import project.project1.user.SiteUser;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@Setter
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private SiteUser user;

    @ManyToOne
    private Goal goal;

    @Enumerated(EnumType.STRING)
    private CertificationType type; // TEXT, IMAGE, VIDEO, EXTERNAL

    private String method; // 예: "GITHUB", "UPLOAD", "NOTION"

    private LocalDateTime certifiedAt;

    private String content; //텍스트 인증시 내용
    private String contentUrl; // 이미지나 영상 URL

    @CreatedDate
    private LocalDateTime createdDate;

}
