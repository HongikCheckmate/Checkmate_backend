package project.project1.api.dto;

public record UserSummaryDto(
        Long id,
        String username,
        String nickname
) {}