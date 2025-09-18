package project.project1.user;

import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN("ROLE_MANAGER"),
    USER("ROLE_USER"),
    GUEST("ROLE_GUEST");

    private final String key;

    UserRole(String key) {
        this.key = key;
    }

}
