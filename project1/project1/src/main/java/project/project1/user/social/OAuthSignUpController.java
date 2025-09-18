package project.project1.user.social;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.project1.user.SiteUser;
import project.project1.user.UserRepository;
import project.project1.user.UserRole;
import project.project1.user.jwt.JwtService;

import java.util.Map;

@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
public class OAuthSignUpController {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest request) {
        SiteUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 추가 정보 업데이트
        user.setPhone_number(request.getPhone_number());
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setRole(UserRole.USER); // 권한 변경
        userRepository.save(user);

        // JWT 재발급
        String accessToken = jwtService.createAccessToken(user.getEmail());
        String refreshToken = jwtService.createRefreshToken();
        jwtService.updateRefreshToken(user.getEmail(), refreshToken);

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "isGuest", false
        ));
    }

    @Data
    static class SignUpRequest {
        private String email;
        private String phone_number;
        private String nickname;
        private String username;
    }
}
