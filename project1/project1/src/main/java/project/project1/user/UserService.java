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
            throw new Exception("이미 존재하는 닉네임입니다.");
        }

        SiteUser user = SiteUser.builder()
                .username(signUpForm.getUsername())
                .password(signUpForm.getPassword())
                .nickname(signUpForm.getNickname())
                .email(signUpForm.getEmail())
                .phone_number(signUpForm.getPhone_number())
                .role(UserRole.USER)
                .build();

        user.passwordEncode(passwordEncoder);
        userRepository.save(user);
        return user;
    }

    public SiteUser findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }
}
