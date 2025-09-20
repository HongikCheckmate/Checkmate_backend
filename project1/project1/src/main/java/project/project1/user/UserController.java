package project.project1.user;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Optional;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @GetMapping("/signup")
    public String signupForm(SignUpForm signUpForm) {
        return "user/signup_form";
    }

    @PostMapping("/signup")
    public String signup(@RequestBody SignUpForm signUpForm, BindingResult bindingResult) throws Exception{
        if (!signUpForm.getCheck_password().equals(signUpForm.getPassword())) {
            bindingResult.rejectValue("check_password", "PasswordInCorrect", "비밀번호가 일치하지 않습니다.");
            return "user/signup_form";
        }

        userService.create(signUpForm);
        return "redirect:/user/login";
    }

    @GetMapping("/login")
    public String login() {
        return "user/login_form";
    }

    @GetMapping("/mypage")
    public String myPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        SiteUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found: " + username));

        UserDataUpdate update = new UserDataUpdate();
        update.setUsername(user.getUsername());
        update.setNickname(user.getNickname());
        update.setPhone_number(user.getPhone_number());
        update.setEmail(user.getEmail());

        model.addAttribute("update", update);
        return "user/myinfo_form";
    }

    @PostMapping("/mypage")
    public String updateMyPage(@AuthenticationPrincipal UserDetails userDetails, @ModelAttribute("update") UserDataUpdate update) {
        String currentuser = userDetails.getUsername();
        SiteUser user = userRepository.findByUsername(currentuser)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found: " + currentuser));

        user.setUsername(update.getUsername());
        user.setNickname(update.getNickname());
        user.setEmail(update.getEmail());
        user.setPhone_number(update.getPhone_number());

        userRepository.save(user);
        return "redirect:/user/mypage";
    }
}
