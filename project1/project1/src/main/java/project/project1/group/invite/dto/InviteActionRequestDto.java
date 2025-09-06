package project.project1.group.invite.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InviteActionRequestDto {
    private Long inviteId;
    private String action; // "ACCEPT" or "REJECT"
}