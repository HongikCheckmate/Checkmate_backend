package project.project1.api;

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

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAPIController {

    private final UserRepository userRepository;

    @GetMapping("/nicknames")
    public ResponseEntity<Page<String>> getPagedNicknames(
            @RequestParam(required = false) String search,
            Pageable pageable)
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
