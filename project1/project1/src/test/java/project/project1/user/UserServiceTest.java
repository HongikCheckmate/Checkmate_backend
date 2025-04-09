package project.project1.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void 회원가입_테스트() {
        // given
        String username = "springuser";
        String email = "spring@naver.com";
        String password = "1234";
        String phone_number = "01012345678";

        // when
        SiteUser createdUser = userService.create(username, email, password, phone_number);

        // then
        SiteUser foundUser = userRepository.findById(createdUser.getId()).orElse(null);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo(username);
        assertThat(foundUser.getEmail()).isEqualTo(email);
        assertThat(passwordEncoder.matches(password, foundUser.getPassword())).isTrue(); // 암호화되었는지 확인
        assertThat(foundUser.getPhone_number()).isEqualTo(phone_number);
    }

}
