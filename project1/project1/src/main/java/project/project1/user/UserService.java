package project.project1.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SiteUser create(SignUpForm signUpForm) throws Exception {

        // 1. [WARN 로그] 이메일 중복
        if (userRepository.findByEmail(signUpForm.getEmail()).isPresent()) {
            String message = "이미 존재하는 이메일입니다.";
            log.warn("UserService create: {}. Email: {}", message, signUpForm.getEmail());
            throw new IllegalArgumentException(message);
        }

        // 2. [WARN 로그] 아이디(username) 중복
        if (userRepository.findByUsername(signUpForm.getUsername()).isPresent()) {
            String message = "이미 존재하는 아이디입니다.";
            log.warn("UserService create: {}. Username: {}", message, signUpForm.getUsername());
            throw new IllegalArgumentException(message);
        }

        SiteUser user = new SiteUser();
        user.setUsername(signUpForm.getUsername());
        user.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        user.setNickname(signUpForm.getNickname());
        user.setEmail(signUpForm.getEmail());
        user.setPhone_number(signUpForm.getPhone_number());
        user.setRole(UserRole.USER); // 기본 권한 설정

        userRepository.save(user);

        log.info("새 사용자 생성 완료. Username: {}, UserID: {}", user.getUsername(), user.getId()); // ◀◀ 로그 추가
        return user;
    }

    public SiteUser findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 사용자 ID로 조회 시도: {}", userId); // ◀◀ 로그 추가
                    return new IllegalArgumentException("User not found: " + userId);
                });
    }
}
