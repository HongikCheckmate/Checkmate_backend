package project.project1.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> signup(@RequestBody SignUpForm signUpForm) {
        if (signUpForm.getPassword() == null || signUpForm.getCheck_password() == null) {
            return ResponseEntity.badRequest().body("비밀번호를 입력하세요.");
        }

        if (!Objects.equals(signUpForm.getPassword(), signUpForm.getCheck_password())) {
            return ResponseEntity.badRequest().body("비밀번호가 일치하지 않습니다.");
        }

        try {
            userService.create(signUpForm);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.ok("회원가입이 성공적으로 완료되었습니다.");
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

    @PostMapping("/mypage")
    public ResponseEntity<Void> updateMyPage(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserDataUpdate update) { // @ModelAttribute 대신 @RequestBody
        String currentuser = userDetails.getUsername();
        SiteUser user = userRepository.findByUsername(currentuser)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found: " + currentuser));

        user.setNickname(update.getNickname());
        user.setEmail(update.getEmail());
        user.setPhone_number(update.getPhone_number());
        userRepository.save(user);

        return ResponseEntity.ok().build(); // 성공 응답 (body 없음)
    }
}
