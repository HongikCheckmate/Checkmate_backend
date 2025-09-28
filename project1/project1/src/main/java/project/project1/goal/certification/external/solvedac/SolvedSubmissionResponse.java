package project.project1.goal.certification.external.solvedac;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SolvedSubmissionResponse {
    private List<SolvedSubmission> items;
    private int count;
}
