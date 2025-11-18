package project.project1.group;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import project.project1.api.dto.GroupSummaryDto;
import project.project1.exception.ForbiddenException;
import project.project1.group.dto.GroupCreateRequestDto;
import project.project1.group.dto.GroupUpdateRequestDto;
import project.project1.user.SiteUser;
import project.project1.user.UserRepository;

import java.util.List;

@Controller
@RequestMapping("/group")
public class GroupController {

    private final GroupService groupService;
    private final UserRepository userRepository;

    public GroupController(GroupService groupService, UserRepository userRepository) {
        this.groupService = groupService;
        this.userRepository = userRepository;
    }

    @PatchMapping("/{groupId}")
    public ResponseEntity<?> update(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long groupId,
            @RequestBody @Valid GroupUpdateRequestDto body
    ) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        GroupSummaryDto dto = groupService.updateGroup(groupId, principal.getUsername(), body);
        return ResponseEntity.ok(dto);
    }


    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<?> handleForbidden(ForbiddenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @PostMapping("/create")
    public ResponseEntity<?> createGroup(@AuthenticationPrincipal UserDetails userDetails, @RequestBody GroupCreateRequestDto dto) {
        try{
            groupService.createGroup(dto);
            return ResponseEntity.ok("ok");
        } catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
