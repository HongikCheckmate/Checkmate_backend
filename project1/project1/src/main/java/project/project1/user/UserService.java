package project.project1.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SiteUser create(SignUpForm signUpForm) throws Exception {

        if (userRepository.findByEmail(signUpForm.getEmail()).isPresent()) {
            throw new Exception("이미 존재하는 이메일입니다.");
        }

        if (userRepository.findByUsername(signUpForm.getUsername()).isPresent()) {
            throw new Exception("이미 존재하는 아이디입니다.");
        }

        SiteUser user = new SiteUser();
        user.setUsername(signUpForm.getUsername());
        user.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        user.setNickname(signUpForm.getNickname());
        user.setEmail(signUpForm.getEmail());
        user.setPhone_number(signUpForm.getPhone_number());
        user.setRole(UserRole.USER); // 기본 권한 설정

        userRepository.save(user);
        return user;
    }

    public SiteUser findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }
}
