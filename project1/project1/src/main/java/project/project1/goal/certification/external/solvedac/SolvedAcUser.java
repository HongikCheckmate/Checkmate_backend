package project.project1.goal.certification.external.solvedac;

import jakarta.persistence.*;
import lombok.*;
import project.project1.user.SiteUser;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolvedAcUser {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // SiteUser의 ID를 SolvedAcUser의 ID로 매핑
    private SiteUser siteUser;

    @Column(unique = true)
    private String handle; // 백준 핸들

    // 생성자
    public SolvedAcUser(SiteUser siteUser, String handle) {
        this.siteUser = siteUser;
        this.handle = handle;
    }
}
