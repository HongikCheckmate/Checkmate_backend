package project.project1.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SecurityConfigTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void BeforeEach() {

        SiteUser user = userService.create("testuser", "test@naver.com",
                "test1234", "01012345678");
        userRepository.save(user);
        System.out.println("ID = " + user.getUsername());
        System.out.println("비밀번호: " + user.getPassword());
    }

    @Test
    @DisplayName("로그인 성공")
    public void 로그인_성공_테스트() throws Exception {
        //given
        String username = "testuser";
        String password = "test1234";

        mockMvc.perform(formLogin("/user/login")
                        .user(username).password(password))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));


    }

    @Test
    @DisplayName("아이디 틀려서 로그인 실패")
    public void 로그인_실패_테스트1() throws Exception {
        mockMvc.perform(formLogin("/user/login")
                        .user("wronguser").password("test1234"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/login?error")); // 실패 시 이동
    }
    @Test
    @DisplayName("패스워드 틀려서 로그인 실패")
    public void 로그인_실패_테스트2() throws Exception {
        mockMvc.perform(formLogin("/user/login")
                        .user("testuser").password("wrong1234"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/login?error"));
    }


    @Test
    public void 로그아웃_테스트() throws Exception {
        mockMvc.perform(logout("/user/logout"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/login")); // 로그아웃 성공 시 이동
    }

}
