package project.project1.user.social;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import project.project1.user.UserRepository;
import project.project1.user.UserRole;
import project.project1.user.jwt.JwtService;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CustomAuthorizationRequestRepository authorizationRequestRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        log.info("OAuth2 Login 성공!");
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String accessToken = jwtService.createAccessToken(oAuth2User.getId(), oAuth2User.getUsername());
        String refreshToken = jwtService.createRefreshToken();

        // 신규 유저 여부 체크
        boolean isGuest = oAuth2User.getRole() == UserRole.GUEST;
        String frontendBaseUrl = CookieUtils.getCookie(request, CustomAuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue)
                .orElse((frontendUrl));

        // DB 업데이트 (refreshToken 저장)
        jwtService.updateRefreshToken(oAuth2User.getUsername(), refreshToken);

        String targetPath;
        if (isGuest) {
            targetPath = "/oauth-signup-info"; // GUEST는 정보 입력 폼으로
        } else {
            targetPath = "/"; // USER는 홈으로
        }

        String targetUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl + targetPath)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("isGuest", isGuest)
                .build()
                .toUriString();

        authorizationRequestRepository.clearCookies(request, response);

        response.sendRedirect(targetUrl);

    }
}
