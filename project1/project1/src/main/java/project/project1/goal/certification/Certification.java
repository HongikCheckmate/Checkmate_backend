package project.project1.goal.certification;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long goalId;

    @Enumerated(EnumType.STRING)
    private CertificationType type; // TEXT, IMAGE, VIDEO, EXTERNAL

    private String method; // 예: "GITHUB", "UPLOAD", "NOTION"

    private LocalDateTime certifiedAt;

    private String content; //텍스트 인증시 내용
    private String contentUrl; // 이미지나 영상 URL
}
