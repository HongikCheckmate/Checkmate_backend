package project.project1.goal;

import lombok.Getter;

@Getter
public class GoalResponseDto {
    private Long goalId;
    private String goalName;
    private String message;

    // 생성자: 엔티티를 받아서 DTO로 변환
    public GoalResponseDto(Goal goal) {
        this.goalId = goal.getId();
        this.goalName = goal.getName();
        this.message = "목표가 성공적으로 생성되었습니다.";
    }
}
