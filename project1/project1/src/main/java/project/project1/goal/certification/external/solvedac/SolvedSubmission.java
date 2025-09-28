package project.project1.goal.certification.external.solvedac;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SolvedSubmission {
    private int problemId;
    private String result; // 예: "AC", "WA"

}
