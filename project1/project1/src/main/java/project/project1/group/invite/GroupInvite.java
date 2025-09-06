package project.project1.group.invite;

import jakarta.persistence.*;
import lombok.*;
import project.project1.group.Group;
import project.project1.user.SiteUser;

@Entity
@Table(name = "group_invites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private SiteUser inviter; // 초대한 사람 (A)

    @ManyToOne
    private SiteUser invitee; // 초대받은 사람 (B)

    @ManyToOne
    private Group group;

    @Enumerated(EnumType.STRING)
    private InviteStatus status = InviteStatus.PENDING;

}
