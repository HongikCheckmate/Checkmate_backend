package project.project1.user.social;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.Base64;

@Component
public class CustomAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    private static final String COOKIE_NAME = "oauth2_auth_request";
    private final ObjectMapper objectMapper = new ObjectMapper();
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";


    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return CookieUtils.getCookie(request, COOKIE_NAME)
                .map(cookie -> deserialize(cookie.getValue()))
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {

        if (authorizationRequest == null) {
            CookieUtils.deleteCookie(request, response, COOKIE_NAME);
            CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
            return;
        }

        // 요청에서 redirect_uri 파라미터 추출
        String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (redirectUriAfterLogin != null) {
            CookieUtils.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin, 180);
        }

        // 기존 OAuth2AuthorizationRequest 저장
        String value = serialize(authorizationRequest);
        CookieUtils.addCookie(response, COOKIE_NAME, value, 180);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        OAuth2AuthorizationRequest requestData = loadAuthorizationRequest(request);
        CookieUtils.deleteCookie(request, response, COOKIE_NAME);

        return requestData;
    }

    public void clearCookies(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.deleteCookie(request, response, COOKIE_NAME);
        CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
    }

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        return Base64.getUrlEncoder().encodeToString(
                SerializationUtils.serialize(authorizationRequest)
        );
    }

    // 역직렬화: Base64 String -> Byte Array -> Java Object
    private OAuth2AuthorizationRequest deserialize(String value) {
        return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(
                Base64.getUrlDecoder().decode(value)
        );
    }
}
