package project.project1.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Column(unique = true)
    private String solvedAcHandle; // 백준 아이디

    @Enumerated(EnumType.STRING)
    private SocialType socialType; // 소셜타입

    private String refreshToken; // 리프레시 토큰

    public void passwordEncode(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }

    public void updateRefreshToken(String updateRefreshToken) {
        this.refreshToken = updateRefreshToken; // 리프레시 토큰 재발급
    }

}
