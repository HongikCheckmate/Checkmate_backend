package project.project1.group.invite.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import project.project1.group.invite.InviteStatus;

@Getter
@Setter
@Builder
public class InviteResponseDto {
    private Long id;
    private String inviterName;
    private String groupName;
    private InviteStatus status;
}