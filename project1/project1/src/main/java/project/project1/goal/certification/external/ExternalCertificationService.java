package project.project1.goal.certification.external;

public interface ExternalCertificationService {
    boolean verifyCertification(String accountName);
    // 계정명 입력 받아서 오늘 인증했는지 검사
}
