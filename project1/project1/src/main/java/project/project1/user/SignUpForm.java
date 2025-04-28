package project.project1.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpForm {
    @Size(min = 7, max = 25)
    @NotEmpty(message = "ID를 입력하세요.")
    private String username;

    @NotEmpty(message = "비밀번호를 입력하세요.")
    private String password;

    @NotEmpty(message = "닉네임 입력하세요.")
    private String nickname;

    @NotEmpty(message = "이메일을 입력하세요.")
    @Email
    private String email;

    @NotEmpty(message = "전화번호를 입력하세요.")
    private String phone_number;
}
