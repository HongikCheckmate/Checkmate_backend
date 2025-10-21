package project.project1.certification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import project.project1.goal.certification.external.solvedac.SolvedAcCertificationService;
import project.project1.goal.certification.external.solvedac.SolvedAcClient;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class SolvedAcClientIntegrationTest {
    @Autowired
    private SolvedAcClient solvedAcClient;

    @Autowired
    private SolvedAcCertificationService solvedAcCertificationService;

    @Test
    void hasSolvedProblem_실제_API_연동_테스트() {
        // given (준비) - 실제 존재하는 데이터 사용
        String handle = "goddnsdlqhrh"; // solved.ac 운영자 핸들
        List<Integer> targetProblemIds = List.of(1000, 1001); // basta님이 푼 문제들

        // when (실행)
        boolean result = solvedAcCertificationService.verifySpecificProblemsSolved(handle, targetProblemIds);

        // then (검증)
        assertThat(result).isTrue();
    }

    @Test
    void getSolvedCount_실제_API_연동_테스트() {
        // given
        String handle = "goddnsdlqhrh";
        List<Integer> targetProblemIds = List.of(100);

        // when (실행)
        boolean result = solvedAcCertificationService.verifySpecificProblemsSolved(handle, targetProblemIds);

        // then (검증)
        assertThat(result).isFalse();
    }

}
