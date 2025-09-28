package project.project1.goal.certification.external.solvedac;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SolvedAcService {
    private final WebClient webClient = WebClient.create("https://solved.ac/api/v3");

    public List<SolvedSubmission> getRecentSubmissions(String handle, int size) {
        return webClient.get()
                .uri("/user/submission?handle={handle}&size={size}", handle, size)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<SolvedSubmissionResponse>() {})
                .block()
                .getItems();
    }
}
