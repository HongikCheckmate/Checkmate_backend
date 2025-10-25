package project.project1.user.json;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        log.warn("로그인 실패. 이유: {}", exception.getMessage());

        // 3. 클라이언트에게 보낼 응답 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 상태 코드
        response.setContentType("application/json;charset=UTF-8");

        // 4. 클라이언트에게 'null' 대신 구체적인 에러 메시지 전송
        String errorMessage = "아이디 또는 비밀번호가 일치하지 않습니다."; // 사용자에게 보여줄 메시지
        response.getWriter().write("{\"success\": false, \"error\": \"" + errorMessage + "\"}");
    }
}
