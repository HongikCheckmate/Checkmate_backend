package project.project1.group;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "group")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="group_id")
    private Long id;

    @Nonnull
    @Column(name="name")
    private String name;

    @Nonnull
    @Column(name="desc")
    private String description;

    @Column(name="is_public")
    private boolean isPublic;

    @Column(name="password")
    private String password;


}
