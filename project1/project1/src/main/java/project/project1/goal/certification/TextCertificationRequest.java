package project.project1.goal.certification;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TextCertificationRequest {
    private Long userId;
    private Long goalId;
    private String content;// 사용자가 입력한 텍스트 인증 내용
}
