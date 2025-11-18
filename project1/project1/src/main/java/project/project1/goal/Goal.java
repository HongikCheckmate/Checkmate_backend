package project.project1.goal;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import project.project1.goal.certification.CertificationType;
import project.project1.goal.certification.external.ExternalCertificationMethod;
import project.project1.group.Group;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "goal_type") // 자식 타입을 구분할 컬럼
public abstract class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; //목표이름

    @Column(nullable = false)
    private int cycle; //주기

    private LocalDate endDate; //끝나는 날짜

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CertificationType certificationType; //인증방식

    @Column
    @Enumerated(EnumType.STRING)
    private ExternalCertificationMethod externalMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    private Long managerId; //방장아이디

    @CreatedDate
    private LocalDateTime createdDate; // 목표 생성일

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoalMember> members = new ArrayList<>();

    protected Goal(CertificationType type) {
        this.certificationType = type;
    }

    protected Goal(CertificationType type, ExternalCertificationMethod method) {
        this.certificationType = type;
        this.externalMethod = method;
    }
    public void addMember(GoalMember member) {
        this.members.add(member);
        member.setGoal(this);
    }
}
