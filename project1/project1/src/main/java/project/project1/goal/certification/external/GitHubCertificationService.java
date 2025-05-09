package project.project1.goal.certification.external;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
public class GitHubCertificationService implements ExternalCertificationService{

    private final WebClient webClient;

    public GitHubCertificationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.github.com").build();
    }

    @Override
    public boolean verifyCertification(String githubUsername) {
        String url = "/users/" + githubUsername + "/events"; //github 이벤트 가져오기

        try{
            var events = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(GitHubEvent[].class) //JSON 배열로 받아옴
                    .block(); //blocking으로 처리

            if(events == null) {
                return false;
            }
            ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
            String today = nowUtc.toLocalDate().toString();

            for (GitHubEvent event : events) { //오늘 날짜에 푸시 이벤트 있었는지 확인
                if("PushEvent".equals(event.getType()) && event.getCreatedAt().startsWith(today)) {
                    System.out.println("이벤트 타입: " + event.getType() + ", 시간: " + event.getCreatedAt());
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
