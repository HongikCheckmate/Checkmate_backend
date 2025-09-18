package project.project1.user.social;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import project.project1.user.UserRole;

import java.util.Collection;
import java.util.Map;

@Getter
public class CustomOAuth2User extends DefaultOAuth2User { // DefaultOAuth2User를 상속하고, username, email, role 필드 추가
    private String username;
    private String email;
    private UserRole role;

    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attributes, String nameAttributeKey,
                            String email, String username, UserRole role) {
        super(authorities, attributes, nameAttributeKey);
        this.username = username;
        this.role = role;
        this.email = email;
    }
}
