package project.project1.user.social;

import java.util.Map;

public class GoogleOAuth2UserForm extends OAuth2UserForm{
    public GoogleOAuth2UserForm(Map<String, Object> attributes) {
        super(attributes);
    }

//    { 구글 유저 정보 JSON
//        "sub": "식별값",
//            "name": "name",
//            "given_name": "given_name",
//            "picture": "https//lh3.googleusercontent.com/~~",
//            "email": "email",
//            "email_verified": true,
//            "locale": "ko"
//    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getNickname() {
        return (String) attributes.get("name");
    }
}

