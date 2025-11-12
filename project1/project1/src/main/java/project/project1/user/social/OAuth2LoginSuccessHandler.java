package project.project1.user.social;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import project.project1.user.UserRepository;
import project.project1.user.UserRole;
import project.project1.user.jwt.JwtService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        log.info("OAuth2 Login 성공!");
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String accessToken = jwtService.createAccessToken(oAuth2User.getId(), oAuth2User.getUsername());
        String refreshToken = jwtService.createRefreshToken();

        // 신규 유저 여부 체크
        boolean isGuest = oAuth2User.getRole() == UserRole.GUEST;

        // DB 업데이트 (refreshToken 저장)
        jwtService.updateRefreshToken(oAuth2User.getUsername(), refreshToken);

        String frontendBaseUrl = "http://localhost:5173";
        String targetPath = "/oauth/callback";

        String targetUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl + targetPath)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("isGuest", isGuest)
                .build()
                .toUriString();

        clearAuthenticationAttributes(request, response);

        response.sendRedirect(targetUrl);

    }
    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}
