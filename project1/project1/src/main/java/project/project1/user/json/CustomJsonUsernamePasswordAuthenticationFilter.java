package project.project1.user.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class CustomJsonUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private static final String DEFAULT_LOGIN_REQUEST_URL = "/api/user/login"; // "/login"으로 오는 요청을 처리
    private static final String HTTP_METHOD = "POST"; // 로그인 HTTP 메소드는 POST
    private static final String USERNAME_KEY = "username"; // 회원 로그인 시 아이디 요청 JSON Key : "username"
    private static final String PASSWORD_KEY = "password"; // 회원 로그인 시 비밀번호 요청 JSon Key : "password"
    private static final AntPathRequestMatcher DEFAULT_LOGIN_PATH_REQUEST_MATCHER =
            new AntPathRequestMatcher(DEFAULT_LOGIN_REQUEST_URL, HTTP_METHOD); // "/login" + POST로 온 요청에 매칭된다.

    private final ObjectMapper objectMapper;


    public CustomJsonUsernamePasswordAuthenticationFilter(ObjectMapper objectMapper) {
        super(DEFAULT_LOGIN_PATH_REQUEST_MATCHER); // 위에서 설정한 "login" + POST로 온 요청을 처리하기 위해 설정
        this.objectMapper = objectMapper;
        setRequiresAuthenticationRequestMatcher(
                new org.springframework.security.web.util.matcher.AntPathRequestMatcher(DEFAULT_LOGIN_REQUEST_URL, HTTP_METHOD)
        );
    }

    /**
     * 인증 처리 메소드
     * StreamUtils를 통해 request에서 messageBody(JSON) 반환
     * 요청 JSON Example
     * {
     *    "username" : "test1234"
     *    "password" : "test1234"
     * }
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {
        String contentType = request.getContentType();

        String username;
        String password;

        if (contentType != null && contentType.startsWith(MediaType.APPLICATION_JSON_VALUE)) {
            // JSON 방식 처리
            String messageBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            Map<String, String> usernamePasswordMap = objectMapper.readValue(messageBody, Map.class);
            username = usernamePasswordMap.get(USERNAME_KEY);
            password = usernamePasswordMap.get(PASSWORD_KEY);

        } else if (contentType != null && contentType.startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
            // Form 방식 처리
            username = request.getParameter(USERNAME_KEY);
            password = request.getParameter(PASSWORD_KEY);

        } else {
            throw new AuthenticationServiceException("Unsupported Content-Type: " + contentType);
        }
        if (username == null) {
            username = "";
        }
        if (password == null) {
            password = "";
        }

        username = username.trim();

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(username, password);

        return this.getAuthenticationManager().authenticate(authRequest);
    }
}
