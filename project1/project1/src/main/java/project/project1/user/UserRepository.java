package project.project1.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import project.project1.user.social.SocialType;

import java.util.Optional;

public interface UserRepository extends JpaRepository<SiteUser, Long> {

    Optional<SiteUser> findByUsername(String username);
    Optional<SiteUser> findByEmail(String email);

    Optional<SiteUser> findByRefreshToken(String refreshToken);


    Optional<SiteUser> findBySocialTypeAndSocialId(SocialType socialType, String socialId);
    // 소셜아이디와 타입으로 회원검색 가능

    Page<SiteUser> findByNicknameContainingIgnoreCase(String nickname, Pageable pageable);
    Page<SiteUser> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
    Page<SiteUser> findByNicknameAndUsernameContainingIgnoreCase(String nickname, String username, Pageable pageable);
}
