package project.project1.goal.certification.external;

import project.project1.goal.Goal;
import project.project1.goal.certification.external.solvedac.SolvedSubmission;
import project.project1.user.SiteUser;

import java.util.List;

public interface ExternalCertificationService {
    /**
     * 인증 검증
     * @param handle 백준 아이디
     * @param value  문제번호 또는 갯수
     * @return true = 인증 성공, false = 인증 실패
     */
    boolean verify(String handle, int value);
}
