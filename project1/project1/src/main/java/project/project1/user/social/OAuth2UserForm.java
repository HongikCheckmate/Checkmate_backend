package project.project1.user.social;

import java.util.Map;

public abstract class OAuth2UserForm {
    protected Map<String, Object> attributes;
    public OAuth2UserForm(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    public abstract String getId(); //소셜 식별 값 : 구글 - "sub", 깃허브 - "ID"
    public abstract String getNickname();
}
