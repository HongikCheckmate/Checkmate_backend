package project.project1.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import project.project1.goal.certification.external.github.GithubUser;
import project.project1.goal.certification.external.solvedac.SolvedAcUser;
import project.project1.user.social.SocialType;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SiteUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username; //로그인 ID

    private String password;

    @Column(unique = true)
    private String nickname;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phone_number;

    @Enumerated(value = EnumType.STRING)
    private UserRole role;

    private String socialId; // 소셜ID

    @Enumerated(EnumType.STRING)
    private SocialType socialType; // 소셜타입

    private String refreshToken; // 리프레시 토큰

    public void updateRefreshToken(String updateRefreshToken) {
        this.refreshToken = updateRefreshToken; // 리프레시 토큰 재발급
    }

    @OneToOne(mappedBy = "siteUser", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private SolvedAcUser solvedAcUser;

    @OneToOne(mappedBy = "siteUser", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private GithubUser githubUser;
}
