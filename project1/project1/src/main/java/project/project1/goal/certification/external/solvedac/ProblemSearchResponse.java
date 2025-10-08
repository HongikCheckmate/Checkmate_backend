package project.project1.goal.certification.external.solvedac;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProblemSearchResponse {
    private int count;
    private List<ProblemItem> items;

    @Getter @Setter
    public static class ProblemItem {
        private int problemId;
        private String titleKo;
        private int level;
    }
}
