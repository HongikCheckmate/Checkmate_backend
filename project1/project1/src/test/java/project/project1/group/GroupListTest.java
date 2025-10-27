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
import project.project1.user.SiteUser;
import project.project1.user.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GroupListTest {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        groupRepository.deleteAll();
        userRepository.deleteAll();

        SiteUser a = userRepository.save(SiteUser.builder().username("A유저").nickname("A유저닉네임").build());
        SiteUser b = userRepository.save(SiteUser.builder().username("B유저").nickname("B유저닉네임").build());
        Group g = groupRepository.save(Group.builder().leader(a).name("테스트 그룹").build());
        Group g2 = groupRepository.save(Group.builder().leader(a).name("테스트 그룹2").build());
        Group g3 = groupRepository.save(Group.builder().leader(b).name("테스트 그룹3").build());
    }

    @Test
    void 그룹_리스트_받기() throws Exception {

        mockMvc.perform(get("/api/group/search"))
                .andExpect(status().isOk())
                .andDo(print());
    }

}
