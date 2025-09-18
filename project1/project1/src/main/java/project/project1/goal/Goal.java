package project.project1.goal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import project.project1.goal.certification.CertificationType;
import project.project1.group.Group;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; //목표이름

    @Column(nullable = false)
    private int cycle; //주기

    private LocalDate endDate; //끝나는 날짜

    @Enumerated(EnumType.STRING)
    private CertificationType certificationType; //인증방식

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    private Long managerId; //방장아이디

    @CreatedDate
    private LocalDateTime createdDate; // 목표 생성일
}
