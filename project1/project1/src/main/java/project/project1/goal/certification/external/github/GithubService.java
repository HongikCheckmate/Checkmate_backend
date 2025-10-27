package project.project1.goal.certification.external.github;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import project.project1.goal.Goal;
import project.project1.user.SiteUser;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Slf4j
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

        if (!(goal instanceof GithubGoal)) {
            return false; // 잘못된 목표 타입
        }
        GithubGoal githubGoal = (GithubGoal) goal;
        String targetRepoName = githubGoal.getTargetRepository();

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

        log.info("GitHub 인증 시작. 유저: {}, 레포: {}", githubUsername, targetRepoName);
        log.info("목표 주기 시작 시간: {}", cycleStartInstant);

        // GitHub Events API 호출
        String apiUrl = String.format("https://api.github.com/users/%s/events", githubUsername);
        while (apiUrl != null && !apiUrl.isEmpty()) {
            ResponseEntity<List<Map<String, Object>>> response;
            try {
                log.info("API 호출: {}", apiUrl);
                response = restTemplate.exchange(
                        apiUrl,
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<>() {}
                );
            } catch (HttpClientErrorException e) {
                // 401 (토큰 오류), 404 등 API 호출 실패
                log.error("API 인증 실패 (4xx): {}", e.getMessage());
                return false;
            }

            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("API 호출 실패: {}", response.getStatusCode());
                return false;
            }

            List<Map<String, Object>> events = response.getBody();
            if (events == null || events.isEmpty()) {
                break; // 이벤트가 더 이상 없으면 중지
            }

            boolean shouldCheckNextPage = true; // 다음 페이지를 확인할지 여부

            for (Map<String, Object> event : events) {

                String createdAtString = (String) event.get("created_at");
                Instant eventInstant = Instant.parse(createdAtString);


                if (eventInstant.isBefore(cycleStartInstant)) {
                    log.info("오래된 이벤트 발견. API 탐색 중단. 이벤트 시간: {}", eventInstant);
                    shouldCheckNextPage = false;
                    break; // 이 페이지의 나머지 이벤트와 다음 페이지 모두 중단
                }

                if ("PushEvent".equals(event.get("type"))) {
                    Map<String, Object> repoMap = (Map<String, Object>) event.get("repo");
                    String eventRepoName = (String) repoMap.get("name");

                    log.info("PushEvent 발견. 레포: [{}], 목표 레포: [{}]", eventRepoName, targetRepoName);
                    if (eventRepoName != null && eventRepoName.equalsIgnoreCase(targetRepoName)) {
                        log.info("✅ 인증 성공! 유효한 커밋 발견.");
                        return true;
                    }
                }
            } // (for-loop 종료: 1페이지 처리 완료)

            if (!shouldCheckNextPage) {
                break; // (while-loop 중단: 오래된 이벤트를 만나서 종료)
            }

            // (수정) 다음 페이지 URL을 Link 헤더에서 가져옴
            List<String> linkHeader = response.getHeaders().get("Link");
            apiUrl = getNextPageUrl(linkHeader);

        } // (while-loop 종료: 모든 페이지 확인 완료)
        // --- 페이징 처리 종료 ---

        log.warn("❌ 인증 실패. 유효한 커밋을 찾지 못함.");
        return false;
    }

    private String getNextPageUrl(List<String> linkHeader) {
        if (linkHeader == null) {
            return null;
        }

        // Link 헤더는 보통 [<url1>; rel="next", <url2>; rel="last"] 형식입니다.
        String links = linkHeader.get(0);
        String[] parts = links.split(",");

        for (String part : parts) {
            if (part.contains("rel=\"next\"")) {
                int start = part.indexOf('<');
                int end = part.indexOf('>');
                return part.substring(start + 1, end);
            }
        }
        return null; // 'next' 링크가 없으면 null 반환
    }
}
