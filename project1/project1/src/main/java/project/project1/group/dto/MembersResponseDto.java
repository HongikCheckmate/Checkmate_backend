package project.project1.group.dto;

import java.util.List;

public record MembersResponseDto(
        long size,
        List<GroupMemberSummaryDto> users
) {}