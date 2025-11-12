package project.project1.group.dto;

import java.time.LocalDateTime;

public record MemberSummaryDto(
        Long id,
        String username,
        String nickname,
        LocalDateTime joinDate,
        LocalDateTime submitDate
) {}