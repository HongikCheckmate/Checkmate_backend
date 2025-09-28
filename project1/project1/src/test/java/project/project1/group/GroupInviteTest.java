package project.project1.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import project.project1.group.invite.dto.*;
import project.project1.user.SiteUser;
import project.project1.user.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GroupInviteTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    private Long inviterId;
    private Long inviteeId;
    private Long groupId;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        groupRepository.deleteAll();

        SiteUser a = userRepository.save(SiteUser.builder().username("A유저").build());
        SiteUser b = userRepository.save(SiteUser.builder().username("B유저").build());
        Group g = groupRepository.save(Group.builder().leader(a).name("테스트 그룹").build());

        inviterId = a.getId();
        inviteeId = b.getId();
        groupId = g.getId();
    }

    @Test
    void 초대_조회_수락() throws Exception {
        // 1. 초대 생성
        InviteCreateRequestDto createDto = new InviteCreateRequestDto();
        createDto.setInviterId(inviterId);
        createDto.setInviteeId(inviteeId);
        createDto.setGroupId(groupId);

        String createResponse = mockMvc.perform(post("/api/invites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        // 생성된 초대 ID 추출
        Long inviteId = objectMapper.readTree(createResponse).get("id").asLong();

        // 2. B가 초대 목록 확인
        mockMvc.perform(get("/api/invites/" + inviteeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].inviterName").value("A유저"))
                .andExpect(jsonPath("$[0].groupName").value("테스트 그룹"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        // 3. B가 초대 수락
        InviteActionRequestDto actionDto = new InviteActionRequestDto();
        actionDto.setInviteId(inviteId);
        actionDto.setAction("ACCEPT");

        mockMvc.perform(post("/api/invites/action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actionDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }
}