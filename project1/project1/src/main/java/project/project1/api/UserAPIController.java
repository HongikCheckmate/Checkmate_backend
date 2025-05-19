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
import project.project1.user.SiteUser;
import project.project1.user.UserRepository;

@Tag(name = "User API", description = "유저 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAPIController {

    private final UserRepository userRepository;

    @Operation(summary = "유저 닉네임 목록 조회",
                    description = "유저의 닉네임 목록을 반환합니다. 검색어 필터링이 가능합니다.")
    @GetMapping("/nicknames")
    public ResponseEntity<Page<String>> getPagedNicknames(
            @Parameter(description = "닉네임 검색 키워드(부분 일치)") @RequestParam(required = false) String search,
            @Parameter(hidden = true) Pageable pageable)
    {
        Page<String> nicknamePage;

        if (search != null && !search.isEmpty()) {
            nicknamePage = userRepository.findByNicknameContainingIgnoreCase(search, pageable)
                    .map(SiteUser::getNickname);
        } else {
            nicknamePage = userRepository.findAll(pageable)
                    .map(SiteUser::getNickname);
        }

        return ResponseEntity.ok(nicknamePage);
    }

}
