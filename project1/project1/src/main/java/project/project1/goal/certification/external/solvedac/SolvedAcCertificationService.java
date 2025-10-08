package project.project1.goal.certification.external.solvedac;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SolvedAcCertificationService {
    private final SolvedAcClient solvedAcClient;

    /**
     * 특정 기간 내에 목표한 개수만큼 문제를 풀었는지 검증
     * @param requiredCount 목표로 하는 문제 개수
     * @param startCount 스터디 시작 시점에 사용자가 풀었던 문제 총 개수
     * @return 목표 달성 여부 (true/false)
     */
    public boolean verifyNProblemsSolvedInPeriod(String handle, int requiredCount, int startCount) {
        // 1. SolvedAcClient를 통해 현재까지 푼 문제 총 개수를 가져옵니다.
        int currentCount = solvedAcClient.getSolvedCount(handle);

        // 2. (현재 푼 개수 - 시작 시점 푼 개수)로 기간 내 푼 문제 수를 계산합니다.
        int solvedInPeriod = currentCount - startCount;

        // 3. 기간 내 푼 문제 수가 목표치 이상인지 확인합니다.
        return solvedInPeriod >= requiredCount;
    }

    /**
     * 목표로 지정된 특정 문제들을 모두 풀었는지 검증
     * @param targetProblemIds 목표로 지정된 문제 번호 리스트
     * @return 목표 달성 여부 (true/false)
     */
    public boolean verifySpecificProblemsSolved(String handle, List<Integer> targetProblemIds) {
        // 목표 문제 리스트를 순회합니다.
        for (int problemId : targetProblemIds) {
            // SolvedAcClient를 통해 각 문제를 풀었는지 확인합니다.
            // 하나라도 풀지 않은 문제가 있다면, 즉시 false를 반환하고 검증을 중단합니다.
            if (!solvedAcClient.hasSolvedProblem(handle, problemId)) {
                return false;
            }
        }
        // 모든 문제가 검증을 통과했다면(루프가 중단되지 않았다면), true를 반환합니다.
        return true;
    }
}
