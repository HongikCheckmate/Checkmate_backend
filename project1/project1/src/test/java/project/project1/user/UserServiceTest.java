package project.project1.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
public class SignUpTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void 회원가입_테스트() {
        // given
        String username = "springuser";
        String email = "spring@naver.com";
        String password = "1234";
        String phoneNumber = "01012345678";

        // when
        SiteUser createdUser = userService.create(username, email, password, phoneNumber);

        // then
        SiteUser foundUser = userRepository.findById(createdUser.getId()).orElse(null);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo(username);
        assertThat(foundUser.getEmail()).isEqualTo(email);
        assertThat(foundUser.getPhone_number()).isEqualTo(phoneNumber);
        assertThat(foundUser.getPassword()).isNotEqualTo(password); // 암호화되었는지 확인
    }
}
