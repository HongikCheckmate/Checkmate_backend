package project.project1.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;


    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpForm signUpForm) {
        log.info("회원가입 시도. Username: {}, Email: {}",
                signUpForm.getUsername(), signUpForm.getEmail());

        // 2. [WARN 로그] 비밀번호 유효성 검사 실패
        if (signUpForm.getPassword() == null || signUpForm.getCheck_password() == null) {
            log.warn("비밀번호 누락. Username: {}", signUpForm.getUsername());
            // ◀◀ 일관된 JSON 에러 응답
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "비밀번호를 입력하세요."));
        }

        // 3. [WARN 로그] 비밀번호 불일치
        if (!Objects.equals(signUpForm.getPassword(), signUpForm.getCheck_password())) {
            log.warn("비밀번호 불일치. Username: {}", signUpForm.getUsername());
            // ◀◀ 일관된 JSON 에러 응답
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "비밀번호가 일치하지 않습니다."));
        }

        try {
            SiteUser user = userService.create(signUpForm);

            // 5. [INFO 로그] 회원가입 성공
            log.info("회원가입 성공. Username: {}, UserID: {}", user.getUsername(), user.getId());
            return ResponseEntity.ok(Map.of("success", true, "message", "회원가입이 성공적으로 완료되었습니다."));

        } catch (IllegalArgumentException e) {
            // 4. [WARN 로그] Service에서 발생한 예외 (중복 등)
            // ◀◀ e.getMessage()는 서버 로그에만 남깁니다.
            log.warn("회원가입 실패. Username: {}. 이유: {}", signUpForm.getUsername(), e.getMessage());

            // ◀◀ 사용자에게는 보안 처리된 메시지와 JSON 응답을 보냅니다.
            // 409 Conflict는 '중복 데이터'에 대한 표준 HTTP 상태 코드입니다.
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            // 6. [ERROR 로그] 예측하지 못한 서버 내부 오류
            log.error("회원가입 중 알 수 없는 서버 오류 발생. Username: {}", signUpForm.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "서버 내부 오류가 발생했습니다."));
        }
    }

    @GetMapping("/mypage")
    public ResponseEntity<UserDataUpdate> myPage(@AuthenticationPrincipal UserDetails userDetails) {
        // userDetails가 null인 경우를 대비한 방어 코드 (JwtAuthenticationFilter가 잘 동작하면 필요 없을 수 있음)
        if (userDetails == null) {
            // Spring Security 설정이 잘 되어있다면 이 코드는 실행되지 않음
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        String username = userDetails.getUsername();
        SiteUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found: " + username));

        UserDataUpdate update = new UserDataUpdate();
        update.setUsername(user.getUsername());
        update.setNickname(user.getNickname());
        update.setPhone_number(user.getPhone_number());
        update.setEmail(user.getEmail());

        return ResponseEntity.ok(update);
    }

    @PutMapping("/mypage")
    public ResponseEntity<Map<String, Object>> updateMyPage(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserDataUpdate update) { // @ModelAttribute 대신 @RequestBody
        String currentuser = userDetails.getUsername();
        SiteUser user = userRepository.findByUsername(currentuser)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found: " + currentuser));

        user.setNickname(update.getNickname());
        user.setEmail(update.getEmail());
        user.setPhone_number(update.getPhone_number());
        userRepository.save(user);

        Map<String, Object> responseBody = new HashMap<>();

        responseBody.put("username", user.getUsername());

        return ResponseEntity.ok(responseBody);
    }
}
