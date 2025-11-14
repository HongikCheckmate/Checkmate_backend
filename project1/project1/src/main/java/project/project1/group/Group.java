package project.project1.group;

import jakarta.persistence.*;
import lombok.*;
import project.project1.CryptoConverter;
import project.project1.user.SiteUser;

import java.util.HashSet;
import java.util.Set;

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

    // 한 그룹에 여러 회원이 가입할 수 있음 (다대다)
    @ManyToMany
    @JoinTable(
            name = "group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id")
    )

    @Builder.Default
    private Set<SiteUser> members = new HashSet<>();

    public Set<SiteUser> getMember() {
        return members;
    }

    public void addMember(SiteUser member) {
        members.add(member);
    }

    @PrePersist
    private void addLeaderToMembers(){
        if(leader != null){
            if(members == null)
                members = new HashSet<>();
            members.add(leader);
        }
    }
}
