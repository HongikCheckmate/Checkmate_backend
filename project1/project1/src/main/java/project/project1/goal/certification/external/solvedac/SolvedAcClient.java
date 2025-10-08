package project.project1.goal.certification.external.solvedac;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class SolvedAcClient {
    private final WebClient webClient = WebClient.create("https://solved.ac/api/v3");

    // 특정 문제 풀었는지 확인
    public boolean hasSolvedProblem(String handle, int problemId) {
        ProblemSearchResponse response = webClient.get()
                .uri("/search/problem?query=solved_by:{handle}+id:{id}", handle, problemId)
                .retrieve()
                .bodyToMono(ProblemSearchResponse.class)
                .block();

        return response != null && response.getCount() > 0;
    }

    // 문제 총 푼 개수 확인
    public int getSolvedCount(String handle) {
        UserProblemStatsResponse response = webClient.get()
                .uri("/user/show?handle={handle}", handle)
                .retrieve()
                .bodyToMono(UserProblemStatsResponse.class)
                .block();

        return response != null ? response.getSolvedCount() : 0;
    }
}
