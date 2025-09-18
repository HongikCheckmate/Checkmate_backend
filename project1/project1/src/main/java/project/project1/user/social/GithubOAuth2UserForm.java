package project.project1.user.social;

import java.util.Map;

public class GithubOAuth2UserForm extends OAuth2UserForm{
    public GithubOAuth2UserForm(Map<String, Object> attributes) {
        super(attributes);
    }


    @Override
    public String getId() {
        return (String) attributes.get("id");
    }

    @Override
    public String getNickname() {
        return (String) attributes.get("name");
    }

}

