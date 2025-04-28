package project.project1.group;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import project.project1.user.UserRepository;

import java.util.List;

@Controller
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;
    private final UserRepository userRepository;

    public GroupController(GroupService groupService, UserRepository userRepository) {
        this.groupService = groupService;
        this.userRepository = userRepository;
    }

    // 그룹 생성 폼 보여주기
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("groupForm", new GroupForm());

        // 방장 선택을 위해 모든 회원 목록을 전달 (테스트용)
        model.addAttribute("members", userRepository.findAll());
        return "group/create";
    }

    // 그룹 생성 처리
    @PostMapping("/create")
    public String createGroup(@ModelAttribute("groupForm") GroupForm groupForm, BindingResult result) {
        if (result.hasErrors()) {
            return "group/create";
        }
        groupService.createGroup(
                groupForm.getName(),
                groupForm.getDescription(),
                groupForm.getPassword(),
                groupForm.getManagerId()
        );
        return "redirect:/groups/list"; // 이후 그룹 목록 페이지로 리디렉션 (추가 개발 필요)
    }

    // 그룹 가입 폼 보여주기
    @GetMapping("/join")
    public String showJoinForm(Model model) {
        model.addAttribute("joinForm", new GroupJoinForm());
        return "group/join";
    }

    // 그룹 가입 처리
    @PostMapping("/join")
    public String joinGroup(@ModelAttribute("joinForm") GroupJoinForm joinForm, BindingResult result) {
        if (result.hasErrors()) {
            return "group/join";
        }
        groupService.joinGroup(joinForm.getGroupId(), joinForm.getMemberId());
        return "redirect:/groups/list";
    }

    @GetMapping("/list")
    public String showGroupList(Model model) {
        model.addAttribute("groups", groupService.findAllGroups());
        return "group/list";
    }

    // 그룹 검색
    @GetMapping("/search")
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
