package project.project1.user.social;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import project.project1.user.SiteUser;
import project.project1.user.UserRole;

import java.util.Map;
import java.util.UUID;

@Getter
public class OAuthAttributes { // 소셜마다 들어오는 데이터가 달라서 데이터 분기처리 DTO
    private String nameAttributeKey; // OAuth2 로그인 진행 시 키가 되는 필드 값, PK와 같은 의미
    private OAuth2UserForm oAuth2UserForm; // 소셜 타입별 로그인 유저 정보(닉네임, 이메일, 프로필 사진 등등)

    @Builder
    private OAuthAttributes(String nameAttributeKey, OAuth2UserForm oAuth2UserForm) {
        this.nameAttributeKey = nameAttributeKey;
        this.oAuth2UserForm = oAuth2UserForm;
    }

    public static OAuthAttributes of(SocialType socialType,
                                     String userNameAttributeName, Map<String, Object> attributes) {

        if (socialType == SocialType.GOOGLE) {
            return ofGoogle(userNameAttributeName, attributes);
        } else if (socialType == SocialType.GITHUB) {
            return ofGithub(userNameAttributeName, attributes);
        } else {
            throw new IllegalArgumentException("Unsupported social type: " + socialType);
        }
    }

    public static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oAuth2UserForm(new GoogleOAuth2UserForm(attributes))
                .build();
    }

    public static OAuthAttributes ofGithub(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oAuth2UserForm(new GithubOAuth2UserForm(attributes))
                .build();
    }

    /**
     * of메소드로 OAuthAttributes 객체가 생성되어, 유저 정보들이 담긴 OAuth2UserForm이 소셜 타입별로 주입된 상태
     * OAuth2UserInfo에서 socialId(식별값), nickname 가져와서 build
     * ID에는 UUID로 중복 없는 랜덤 값 생성
     * role은 GUEST로 설정
     */
    public SiteUser toEntity(SocialType socialType, OAuth2UserForm oAuth2UserForm) {
        if (oAuth2UserForm == null) {
            throw new IllegalStateException("OAuth2UserForm is null for socialType: " + socialType);
        }
        return SiteUser.builder()
                .socialType(socialType)
                .socialId(oAuth2UserForm.getId())
                .username(UUID.randomUUID() + "ID")
                .email(UUID.randomUUID() + "@socialUser.com")
                .nickname(oAuth2UserForm.getNickname())
                .role(UserRole.GUEST)
                .build();
    }
}
