package project.project1.group;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import project.project1.group.member.GroupMember;
import project.project1.user.SiteUser;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "groups")
public class Group {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private String description;

    // 그룹 비밀번호(예: 추후 해시 처리할 수 있음)
    @Setter
    @Getter
    private String password;

    @Setter
    @Getter
    private boolean hidden;

    // 그룹의 방장. 한 그룹은 한 명의 방장만 있으므로 ManyToOne 사용.
    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "leader_id", nullable = false)
    private SiteUser leader;

    @Getter
    @Builder.Default
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<GroupMember> groupMembers = new HashSet<>();

    public void addMember(SiteUser user) {
        GroupMember gm = GroupMember.builder()
                .group(this)
                .user(user)
                .joinedAt(LocalDateTime.now())
                .build();
        this.groupMembers.add(gm);
    }

    public void removeMember(SiteUser user) {
        GroupMember target = this.groupMembers.stream()
                .filter(gm -> gm.getUser().equals(user))
                .findFirst()
                .orElse(null);

        if (target != null) {
            target.setGroup(null);
            this.groupMembers.remove(target);
        }
    }
}
