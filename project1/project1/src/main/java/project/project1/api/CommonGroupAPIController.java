package project.project1.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.project1.api.dto.GroupSummaryDto;
import project.project1.group.Group;
import project.project1.group.GroupRepository;
import project.project1.user.SiteUser;
import project.project1.util.QueryParamParser;

@Tag(name = "Common Group API", description = "전체 그룹 관련 API")
@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class CommonGroupAPIController {

    private final GroupRepository groupRepository;

    @Operation(summary = "그룹 목록 조회",
            description = "전체 그룹 목록을 반환합니다. ex) 예: /api/group/search?query=name:hongik_group,nickname:TestLeader")
    @GetMapping("/search")
    public ResponseEntity<Page<GroupSummaryDto>> getGroups(
            @Parameter(description = "name:검색어,nickname:검색어 형태의 문자열. 예) name:홍익,nickname:베포") @RequestParam(required = false) String query,
            @Parameter(hidden = true) Pageable pageable)
    {
        // query 문자열 파싱
        var parsed = QueryParamParser.parseQuery(query);
        String nameFilter = parsed.get("name");
        String nicknameFilter = parsed.get("nickname");

        // DB 검색
        Page<Group> groups = groupRepository.searchByNameAndLeaderNickname(
                nameFilter,
                nicknameFilter,
                pageable
        );

        // DTO 매핑
        Page<GroupSummaryDto> dtoPage = groups.map(g -> new GroupSummaryDto(
                g.getId(),
                g.getLeader().getNickname(),
                g.getName(),
                g.getDescription(),
                g.getMember() != null ? g.getMember().size() : 0
        ));

        return ResponseEntity.ok(dtoPage);
    }

}
