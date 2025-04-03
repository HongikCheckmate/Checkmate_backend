package project.project1.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @GetMapping("/signup")
    public String signup(SignUpForm signUpForm) {
        return "signup_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid SignUpForm signUpForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup_form";
        }
        userService.create(signUpForm.getUsername(),
                signUpForm.getEmail(), signUpForm.getPassword(), signUpForm.getPhone_number());

        return "redirect:/";
    }

}
