package project.project1.goal;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;

    public GoalServiceImpl(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    @Transactional
    @Override
    public Goal createGoal(Goal goal) {
        return goalRepository.save(goal);
    }

    @Override
    public List<Goal> getGoalsByGroupId(Long groupId) {
        return goalRepository.findByGroupId(groupId);
    }

    @Override
    public Goal findById(Long goalId) {
        return goalRepository.findById(goalId)
                .orElseThrow(() -> new NoSuchElementException("해당 목표를 찾을 수 없습니다: id=" + goalId));
    }
}
