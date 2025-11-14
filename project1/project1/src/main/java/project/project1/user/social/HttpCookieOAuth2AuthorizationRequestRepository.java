package project.project1.user.social;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    // 이 쿠키는 OAuth2 인증 요청 정보를 담고 있습니다. (state, redirect_uri 등)
    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";

    // 이 쿠키는 인증이 끝난 후 리다이렉트할 최종 목적지(프론트 주소)를 담습니다.
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "https://checkmate.kimbepo.xyz";

    // 쿠키 만료 시간 (로그인 프로세스 중에만 유효하도록 짧게 설정)
    private static final int COOKIE_EXPIRE_SECONDS = 180; // 3분

    /**
     * 쿠키에서 인증 요청 정보를 불러옵니다.
     */
    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(cookie -> CookieUtils.deserialize(cookie, OAuth2AuthorizationRequest.class))
                .orElse(null);
    }

    /**
     * 인증 요청 정보를 쿠키에 저장합니다.
     */
    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response);
            return;
        }

        // OAuth2 인증 요청 정보를 직렬화하여 쿠키에 저장
        CookieUtils.addCookie(response,
                OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                CookieUtils.serialize(authorizationRequest),
                COOKIE_EXPIRE_SECONDS);

        // 프론트엔드에서 ?redirect_uri=... 로 전달한 최종 목적지 URL을 쿠키에 함께 저장
        String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (StringUtils.hasText(redirectUriAfterLogin)) {
            CookieUtils.addCookie(response,
                    REDIRECT_URI_PARAM_COOKIE_NAME,
                    redirectUriAfterLogin,
                    COOKIE_EXPIRE_SECONDS);
        }
    }

    /**
     * 인증이 완료된 후, 쿠키를 삭제합니다.
     */
    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = this.loadAuthorizationRequest(request);
        removeAuthorizationRequestCookies(request, response);
        return authorizationRequest;
    }

    /**
     * 관련 쿠키들을 모두 삭제합니다.
     */
    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
    }
}
