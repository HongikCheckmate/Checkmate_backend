package project.project1.group.member;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import project.project1.group.dto.GroupMemberSummaryDto;
import project.project1.group.dto.GroupMemberSummaryDto;
import project.project1.group.dto.MembersResponseDto;
import project.project1.util.QueryParamParser;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupMemberService {
    private final GroupMemberRepository groupMemberRepository;


    public boolean isMember(Long groupId, String username) {
        return groupMemberRepository.existsByGroup_IdAndUser_Username(groupId, username);
    }


    public MembersResponseDto listMembers(Long groupId, String query, String sort, boolean reverse, int page) {
        Sort s = buildSort(sort, reverse);
        Pageable pageable = PageRequest.of(Math.max(page, 0), 20, s);


        String nicknameKeyword = QueryParamParser.parseQuery(query).get("name");
        Page<GroupMember> result;
        if (nicknameKeyword == null) {
            result = groupMemberRepository.findByGroup_Id(groupId, pageable);
        } else {
            result = groupMemberRepository.findByGroup_IdAndUser_NicknameContainingIgnoreCase(groupId, nicknameKeyword, pageable);
        }


        List<GroupMemberSummaryDto> users = result.getContent().stream()
                .map(m -> new GroupMemberSummaryDto(
                        m.getUser().getId(),
                        m.getUser().getUsername(),
                        m.getUser().getNickname(),
                        m.getJoinedAt(),
                        m.getLastSubmissionAt()
                ))
                .toList();


        return new MembersResponseDto(result.getTotalElements(), users);
    }


    private Sort buildSort(String sort, boolean reverse) {
        String key = (sort == null) ? "abc" : sort.trim().toLowerCase();
        Sort s;
        switch (key) {
            case "join" -> s = Sort.by("joinedAt");
            case "submission" -> s = Sort.by("lastSubmissionAt");
            default -> s = Sort.by("user.nickname"); // 기본: 닉네임 abc
        }
        return reverse ? s.descending() : s.ascending();
    }
}