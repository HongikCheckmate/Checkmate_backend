package project.project1.goal.certification.external.github;

import jakarta.persistence.*;
import lombok.*;
import project.project1.CryptoConverter;
import project.project1.user.SiteUser;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GithubUser {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // SiteUser의 ID를 GithubUser의 ID로 매핑
    private SiteUser siteUser;

    private String githubUsername;

    @Column(length = 1000)
    @Convert(converter = CryptoConverter.class)
    private String githubAccessToken;
}
