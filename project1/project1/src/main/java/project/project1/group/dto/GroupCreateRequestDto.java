package project.project1.group.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupCreateRequestDto {

    private String name;
    private String description;
    private String password;
    private String roomManager;
    private boolean hidden;

}
