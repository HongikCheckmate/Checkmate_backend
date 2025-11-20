package project.project1.user.social;

import io.sentry.Sentry;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("소셜 로그인 실패! 서버 로그를 확인해주세요.");
        log.info("소셜 로그인에 실패했습니다. 에러 메시지 : {}", exception.getMessage());
        exception.printStackTrace();
        System.out.println("OAuth2 Fail Message: " + exception.getMessage());

        // 2. Sentry에 명시적으로 전송 (중요!)
        Sentry.captureException(exception);

        // 3. 기존 리다이렉트 또는 응답 로직
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "소셜 로그인 실패");
    }
}
