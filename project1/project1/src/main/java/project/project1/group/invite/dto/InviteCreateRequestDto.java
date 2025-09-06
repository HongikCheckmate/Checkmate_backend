package project.project1.group.invite.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InviteCreateRequestDto {
    private Long inviterId;
    private Long inviteeId;
    private Long groupId;
}