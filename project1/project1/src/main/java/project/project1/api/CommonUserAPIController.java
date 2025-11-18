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
import project.project1.api.dto.UserSummaryDto;
import project.project1.user.SiteUser;
import project.project1.user.UserRepository;

@Tag(name = "Common User API", description = "전체 유저 관련 API")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class CommonUserAPIController {

    private final UserRepository userRepository;

    @Operation(summary = "유저 검색",
                    description = "유저 목록을 반환합니다. 닉네임/유저네임 필터링이 가능합니다.")
    @GetMapping("/search")
    public ResponseEntity<Page<UserSummaryDto>> getPagedNicknames(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String username,
            @Parameter(hidden = true) Pageable pageable)
    {
        Page<SiteUser> siteUserPage;

        if(nickname != null && username != null){
            siteUserPage = userRepository.findByNicknameAndUsernameContainingIgnoreCase(nickname, username, pageable);
        } else if(nickname != null){
            siteUserPage = userRepository.findByNicknameContainingIgnoreCase(nickname, pageable);
        } else if(username != null){
            siteUserPage = userRepository.findByUsernameContainingIgnoreCase(username, pageable);
        } else {
            siteUserPage = userRepository.findAll(pageable);
        }

        Page<UserSummaryDto> userSummaryPage = siteUserPage.map(siteUser -> new UserSummaryDto(siteUser.getId(), siteUser.getUsername(), siteUser.getNickname()));

        return ResponseEntity.ok(userSummaryPage);
    }

}
