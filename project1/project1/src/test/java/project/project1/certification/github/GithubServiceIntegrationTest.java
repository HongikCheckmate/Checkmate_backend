package project.project1.certification.github;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import project.project1.goal.Goal;
import project.project1.goal.GoalRepository;
import project.project1.goal.certification.external.github.GithubGoal;
import project.project1.goal.certification.external.github.GithubService;
import project.project1.goal.certification.external.github.GithubUser;
import project.project1.goal.certification.external.github.GithubUserRepository;
import project.project1.user.SiteUser;
import project.project1.user.UserRepository;
import project.project1.user.UserRole;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest // 스프링 애플리케이션 컨텍스트를 모두 로드
@Transactional  // 테스트 후 DB 변경사항을 롤백하여 테스트 격리성 보장
@ActiveProfiles("test") // test 프로파일을 활성화하여 application-test.properties를 사용
public class GithubServiceIntegrationTest {
    @Autowired
    private GithubService githubService;

    @Autowired
    private GithubUserRepository githubUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoalRepository goalRepository;

    // application-test.properties에서 주입받은 값
    @Value("${github.test.username}")
    private String testUsername;

    @Value("${github.test.token}")
    private String testToken;

    private SiteUser user;
    private GithubUser githubUser;
    private Goal goal;

    @BeforeEach
    void setUp() {
        // DB에 테스트용 데이터를 미리 저장
        user = userRepository.save(SiteUser.builder().username("integration_user").role(UserRole.USER).build());
        githubUser = githubUserRepository.save(GithubUser.builder()
                .siteUser(user)
                .githubUsername(testUsername)
                .githubAccessToken(testToken) // 실제 유효한 토큰
                .build());
        GithubGoal githubGoal = new GithubGoal();

        githubGoal.setCreatedDate(LocalDateTime.now().minusDays(1)); // 어제부터 시작된 목표
        githubGoal.setCycle(3); // 주기는 3일

        githubGoal.setTargetRepository(testUsername + "/test-repo"); // 예시 값

        goal = goalRepository.save(githubGoal);

        user.setGithubUser(githubUser); // 연관관계 설정
    }

    @Test
    @DisplayName("실제 GitHub API와 통신하여 최근 커밋이 있으면 true를 반환한다")
    void verify_WithRealApi_Success() {
        // given
        // (중요) 이 테스트를 실행하기 전, 위에서 설정한 GITHUB_TEST_USERNAME 계정으로
        // 아무 레포지토리에나 실제 커밋을 Push 해야 합니다.

        // when
        boolean result = githubService.verify(githubUser, goal);

        // then
        assertThat(result).isTrue();
    }
}
