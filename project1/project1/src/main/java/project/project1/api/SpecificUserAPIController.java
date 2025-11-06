package project.project1.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.project1.user.SiteUser;

@Tag(name = "Specific User API", description = "특정 유저 관련 API")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class SpecificUserAPIController {

    @Operation(summary = "어떤 유저의 초대 목록",
            description = "유저가 그룹들로부터 받은 초대 목록을 반환합니다.")
    @GetMapping("/invites")
    public ResponseEntity<Page<String>> getUserInvitation(
            @AuthenticationPrincipal UserDetails user,
            @Parameter(hidden = true) Pageable pageable)
    {
        return ResponseEntity.ok(null);
    }

}
