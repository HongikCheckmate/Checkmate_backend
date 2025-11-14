package project.project1.group.dto;

import jakarta.validation.constraints.Size;

public record GroupUpdateRequestDto(
        @Size(min = 1, max = 50, message = "그룹명은 1~50자") String name,
        @Size(max = 200, message = "설명은 200자 이내") String description,
        String leader // 숫자 문자열(ID) 또는 username
) {}