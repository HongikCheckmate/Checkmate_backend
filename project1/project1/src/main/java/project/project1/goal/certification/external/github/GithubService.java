package project.project1.goal.certification.external.github;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import project.project1.goal.Goal;
import project.project1.user.SiteUser;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GithubService {
    private final RestTemplate restTemplate;
    private final DateTimeFormatter flexibleDateTimeFormatter;

    public boolean verify(GithubUser githubUser, Goal goal) {
        String githubUsername = githubUser.getGithubUsername();
        String accessToken = githubUser.getGithubAccessToken(); // 복호화 로직 필요

        // HTTP 헤더 설정 (가장 중요!)
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 현재 주기의 시작 날짜 계산 (getCertificationsGroupedByCycle 로직 응용)
        LocalDate goalStart = goal.getCreatedDate().toLocalDate();
        LocalDate today = LocalDate.now(ZoneOffset.UTC); // UTC 기준 오늘 날짜
        int cycleDays = goal.getCycle();

        long daysSinceGoalStart = ChronoUnit.DAYS.between(goalStart, today);
        int currentCycleCount = (int) (daysSinceGoalStart / cycleDays); // 현재 몇 번째 주기인지 (0부터 시작)

        LocalDate cycleStartDate = goalStart.plusDays((long) currentCycleCount * cycleDays);

        // GitHub API는 ZonedDateTime(ISO 8601 형식)을 사용하므로 변환
        ZonedDateTime cycleStartDateTime = cycleStartDate.atStartOfDay(ZoneOffset.UTC);
        Instant cycleStartInstant = cycleStartDateTime.toInstant();

        // GitHub Events API 호출
        String apiUrl = String.format("https://api.github.com/users/%s/events", githubUsername);
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            // API 호출 실패 (토큰 만료, 권한 없음 등)
            // throw new CustomException("GitHub API 호출에 실패했습니다.");
            return false;
        }

        List<Map<String, Object>> events = response.getBody();


        // 현재 주기 내에 커밋(PushEvent)이 있었는지 확인
        for (Map<String, Object> event : events) {
            if ("PushEvent".equals(event.get("type"))) {
                // API 응답의 created_at(예: "2025-10-14T15:22:41Z")을 Instant로 파싱
                String createdAtString = (String) event.get("created_at");
                Instant commitInstant = Instant.parse(createdAtString);
                // 커밋 시간이 현재 주기의 시작 시간보다 이후인지 확인
                if (!commitInstant.isBefore(cycleStartInstant)) {
                    // 주기 내의 커밋을 발견하면 즉시 true 반환
                    return true;
                }
            }
        }

        // 오늘 날짜의 커밋이 없으면 false 반환
        return false;
    }
}
