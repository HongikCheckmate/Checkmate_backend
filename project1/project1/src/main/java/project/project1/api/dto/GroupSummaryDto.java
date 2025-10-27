package project.project1.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class GroupSummaryDto {

    private long id;
    private String leaderNickname;
    private String name;
    private String description;
    private int memberCount;

}
