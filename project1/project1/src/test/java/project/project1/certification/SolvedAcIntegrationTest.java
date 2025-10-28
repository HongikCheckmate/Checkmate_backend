package project.project1.certification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import project.project1.goal.certification.external.solvedac.SolvedAcCertificationService;
import project.project1.goal.certification.external.solvedac.SolvedSubmission;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
public class SolvedAcIntegrationTest {
    @Autowired
    private SolvedAcCertificationService solvedAcService;

    @Test
    void testApiCall() {
//        List<SolvedSubmission> submissions = solvedAcService.getRecentSubmissions("tourist", 10);
//        assertFalse(submissions.isEmpty());
//        System.out.println(submissions.get(0).getProblemId());
    }
}
