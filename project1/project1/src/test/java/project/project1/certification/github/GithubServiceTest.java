package project.project1.certification.github;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import project.project1.goal.Goal;
import project.project1.goal.certification.external.github.GithubGoal;
import project.project1.goal.certification.external.github.GithubService;
import project.project1.goal.certification.external.github.GithubUser;
import project.project1.user.SiteUser;
import project.project1.user.UserRole;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GithubServiceTest {
    @Mock
    private RestTemplate restTemplate;

    private DateTimeFormatter flexibleDateTimeFormatter;
    private GithubService githubService;

    private GithubUser testGithubUser;
    private Goal testGoal; // 부모 타입으로 선언하는 것은 좋습니다 (다형성)

    @BeforeEach
    void setUp() {
        flexibleDateTimeFormatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                .toFormatter();

        githubService = new GithubService(restTemplate, flexibleDateTimeFormatter);

        SiteUser testSiteUser = SiteUser.builder()
                .id(1L)
                .username("test-user-for-github")
                .role(UserRole.USER)
                .build();

        testGithubUser = GithubUser.builder()
                .siteUser(testSiteUser)
                .githubUsername("testUser")
                .githubAccessToken("test_token")
                .build();

        GithubGoal githubGoal = new GithubGoal(); // 2. 자식 클래스(GithubGoal)로 생성

        githubGoal.setCreatedDate(ZonedDateTime.now(ZoneId.of("Asia/Seoul")).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
        githubGoal.setCycle(7);

        githubGoal.setTargetRepository("testUser/testRepo");

        testGoal = githubGoal;
    }

    @Test
    @DisplayName("성공: 현재 주기 내에 커밋(PushEvent)이 있으면 true를 반환한다")
    void verify_success_when_commit_exists_in_cycle() {
        // given
        String nowString = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);

        Map<String, Object> repoData = Map.of("name", "testUser/testRepo");
        Map<String, Object> pushEvent = Map.of(
                "type", "PushEvent",
                "created_at", nowString,
                "repo", repoData // 👈 "repo" 키와 데이터 추가
        );

        List<Map<String, Object>> fakeApiResponse = List.of(pushEvent);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(fakeApiResponse, HttpStatus.OK));

        // when
        boolean result = githubService.verify(testGithubUser, testGoal);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("실패: 주기 내에 커밋(PushEvent)이 없으면 false를 반환한다")
    void verify_fail_when_commit_not_exists_in_cycle() {
        // given
        String oldDateString = ZonedDateTime.now(ZoneOffset.UTC).minusDays(10).format(DateTimeFormatter.ISO_DATE_TIME);

        Map<String, Object> repoData = Map.of("name", "HongikCheckmate_backend/Checkmate_backend");
        Map<String, Object> oldPushEvent = Map.of(
                "type", "PushEvent",
                "created_at", oldDateString,
                "repo", repoData
        );

        List<Map<String, Object>> fakeApiResponse = List.of(oldPushEvent);

        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(fakeApiResponse, HttpStatus.OK));

        // when
        boolean result = githubService.verify(testGithubUser, testGoal);

        // then
        assertThat(result).isFalse();
    }
}
