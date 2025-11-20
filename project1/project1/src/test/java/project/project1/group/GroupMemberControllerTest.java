package project.project1.group;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import project.project1.api.GroupManageAPIController;
import project.project1.group.member.GroupMemberService;
import project.project1.user.UserRepository;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(GroupManageAPIController.class)
class GroupMemberControllerTest {

    //TODO : 테스트 오류 고치기

    @Autowired MockMvc mvc;
    @MockitoBean
    GroupMemberService service;

    @MockitoBean
    GroupRepository groupRepo;

    @MockitoBean
    GroupService groupService;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    GroupManageAPIController groupManageAPIController;

    @MockitoBean
    JpaMetamodelMappingContext jpaMappingContext;


    //@Test
    @DisplayName("그룹 멤버 목록 기본 조회")
    @WithMockUser(username = "khy010802")
    void list_ok() throws Exception {
        BDDMockito.given(service.isMember(1L, "khy010802")).willReturn(true);
        BDDMockito.given(service.listMembers(1L, null, null, false, 0))
                .willReturn(new project.project1.group.dto.MembersResponseDto(0, java.util.List.of()));


        mvc.perform(get("/api/group/1/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(0))
                .andExpect(jsonPath("$.users").isArray());
    }


    //@Test
    @DisplayName("비멤버 접근 금지")
    @WithMockUser(username = "intruder")
    void forbidden_when_not_member() throws Exception {
        BDDMockito.given(service.isMember(1L, "intruder")).willReturn(false);
        mvc.perform(get("/api/group/1/members"))
                .andExpect(status().isForbidden());
    }
}