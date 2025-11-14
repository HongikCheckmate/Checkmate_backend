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

    // 그룹 생성 폼 보여주기
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("groupForm", new GroupForm());

        // 방장 선택을 위해 모든 회원 목록을 전달 (테스트용)
        model.addAttribute("members", userRepository.findAll());
        return "group/create";
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

    @Deprecated(forRemoval = true)
    // 그룹 생성 처리
    //@PostMapping("/create")
    public String createGroup(@AuthenticationPrincipal UserDetails userDetails, @ModelAttribute("groupForm") GroupForm groupForm, BindingResult result) {
        if (result.hasErrors()) {
            return "group/create";
        }
//        groupService.createGroup(
//                groupForm.getName(),
//                groupForm.getDescription(),
//                groupForm.getPassword(),
//                userRepository.findByUsername(userDetails.getUsername()).get().getId()
//        );
        return "redirect:/group/list"; // 이후 그룹 목록 페이지로 리디렉션 (추가 개발 필요)
    }

    // 그룹 가입 폼 보여주기
    // 그룹 검색
    @Deprecated(forRemoval = true)
    // @GetMapping("/join")
    public String showJoinForm(Model model) {
        model.addAttribute("joinForm", new GroupJoinForm());
        return "group/join";
    }
    // 그룹 검색
    @Deprecated(forRemoval = true)
    // @PostMapping("/join")
    public String joinGroup(@ModelAttribute("joinForm") GroupJoinForm joinForm,
                            BindingResult result,
                            @AuthenticationPrincipal UserDetails userDetails) {
        if (result.hasErrors()) {
            return "group/join";
        }

        // 현재 로그인한 사용자의 ID를 조회
        String username = userDetails.getUsername();
        SiteUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found: " + username));

        // memberId는 로그인한 사용자 ID로 자동 설정
        groupService.joinGroup(joinForm.getGroupId(), user.getId());

        return "redirect:/group/list";
    }

    // 그룹 검색
    @Deprecated(forRemoval = true)
    // @GetMapping("/list")
    public String showGroupList(Model model,
                                @PageableDefault(size = 10) Pageable pageable) {
        Page<Group> groupPage = groupService.searchGroupsByName("", pageable); // 전체 그룹 조회
        model.addAttribute("groups", groupPage.getContent());
        model.addAttribute("page", groupPage);
        return "group/list";
    }

    // 그룹 검색
    @Deprecated(forRemoval = true)
    //@GetMapping("/search")
    public String searchGroups(@RequestParam("keyword") String keyword,
                               @PageableDefault(size = 10) Pageable pageable,
                               Model model) {
        Page<Group> searchResults = groupService.searchGroupsByName(keyword, pageable);
        model.addAttribute("groups", searchResults);
        model.addAttribute("page", searchResults);
        model.addAttribute("keyword", keyword);
        return "group/list";  // 그룹 리스트 템플릿 재사용
    }
}
