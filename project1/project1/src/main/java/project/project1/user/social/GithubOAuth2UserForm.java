package project.project1.user.social;

import java.util.Map;

public class GithubOAuth2UserForm extends OAuth2UserForm{
    public GithubOAuth2UserForm(Map<String, Object> attributes) {
        super(attributes);
    }


    @Override
    public String getId() {
        // 깃허브의 id는 Integer 타입이므로 안전하게 문자열로 변환
        Object idObj = attributes.get("id");
        return idObj != null ? String.valueOf(idObj) : null;
    }

    @Override
    public String getNickname() {
        // 깃허브의 "name" 값은 null일 수 있음 → "login" 값으로 fallback 하는 것이 안전
        String name = (String) attributes.get("name");
        if (name == null || name.isBlank()) {
            name = (String) attributes.get("login");
        }
        return name;
    }

}

