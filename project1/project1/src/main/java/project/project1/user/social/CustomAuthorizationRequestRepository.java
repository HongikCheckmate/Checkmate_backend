package project.project1.user.social;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

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
            deleteCookie(request, response, COOKIE_NAME);
            return;
        }

        String value = serialize(authorizationRequest);
        CookieUtils.addCookie(response, COOKIE_NAME, value, 180);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        OAuth2AuthorizationRequest requestData = loadAuthorizationRequest(request);
        deleteCookie(request, response, COOKIE_NAME);
        return requestData;
    }

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        try {
            byte[] data = objectMapper.writeValueAsBytes(authorizationRequest);
            return Base64.getUrlEncoder().encodeToString(data);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to serialize OAuth2AuthorizationRequest", e);
        }
    }

    private OAuth2AuthorizationRequest deserialize(String value) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(value);
            return objectMapper.readValue(decoded, OAuth2AuthorizationRequest.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to deserialize OAuth2AuthorizationRequest", e);
        }
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        CookieUtils.deleteCookie(request, response, name);
    }

    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        deleteCookie(request, response, COOKIE_NAME);
        CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
    }

}
