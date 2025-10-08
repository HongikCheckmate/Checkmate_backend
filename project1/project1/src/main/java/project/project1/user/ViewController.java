package project.project1.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    @GetMapping("/user/login")
    public String loginPage() {
        return "user/login_form"; // templates/user/login_form.html 파일을 렌더링
    }

    @GetMapping("/user/signup")
    public String signupPage() {
        return "user/signup_form"; // templates/user/signup_form.html 파일을 렌더링
    }

    @GetMapping("/user/mypage")
    public String myPage() {
        return "user/myinfo_form"; // templates/user/myinfo_form.html 렌더링
    }

    // 메인 페이지
    @GetMapping("/")
    public String home() {
        return "main"; // templates/main.html 렌더링
    }
}
