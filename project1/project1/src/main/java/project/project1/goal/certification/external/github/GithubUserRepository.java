package project.project1.goal.certification.external.github;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GithubUserRepository extends JpaRepository<GithubUser, Long> {
    Optional<GithubUser> findByGithubUsername(String githubUsername);
}
