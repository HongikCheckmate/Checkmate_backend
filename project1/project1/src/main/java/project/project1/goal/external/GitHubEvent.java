package project.project1.goal.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GitHubEvent {
    private String type;

    @JsonProperty("created_at")
    private String createdAt;
}
