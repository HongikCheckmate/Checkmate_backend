package project.project1.user.social;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import project.project1.user.SiteUser;
import project.project1.user.UserRepository;
import project.project1.user.jwt.JwtService;

import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class OAuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());

        Authentication authentication = authenticationManager.authenticate(authToken);
        String username = authentication.getName();

        SiteUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // [수정 2] User 객체에서 ID와 Role을 각각 꺼냅니다.
        Long userId = user.getId();
        String role = user.getRole().getKey(); // Enum에서 String으로 변환 (.getKey() 또는 .name())

        // [수정 3] 꺼낸 role을 createAccessToken에 전달합니다.
        String accessToken = jwtService.createAccessToken(userId, username, role);
        String refreshToken = jwtService.createRefreshToken();

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        ));
    }
    @Data
    static class LoginRequest {
        private String username;
        private String password;
    }
}
