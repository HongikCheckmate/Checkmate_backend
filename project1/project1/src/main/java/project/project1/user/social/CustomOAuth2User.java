package project.project1.user.social;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import project.project1.user.UserRole;

import java.util.Collection;
import java.util.Map;

@Getter
public class OAuth2User extends DefaultOAuth2User { // 추가정보(이메일, 전화번호 등) 입력 위해 필요
    private String nickname;
    private String email;
    private String phone_number;
    private UserRole role;

    public OAuth2User(Collection<? extends GrantedAuthority> authorities,
                      Map<String, Object> attributes, String nameAttributeKey,
                      String nickname, String email, String phone_number, UserRole role) {
        super(authorities, attributes, nameAttributeKey);
        this.email = email;
        this.nickname = nickname;
        this.phone_number = phone_number;
        this.role = role;
    }
}
