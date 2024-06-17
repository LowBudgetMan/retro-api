package io.nickreuter.retroapi.retro.template;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TemplateControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private List<Template> templates;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void getTemplates_ReturnsListOfTemplates() throws Exception {
        mockMvc.perform(get("/api/templates")
                .with(jwt())
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[0].id").value(templates.getFirst().id()))
            .andExpect(jsonPath("$.[0].name").value(templates.getFirst().name()))
            .andExpect(jsonPath("$.[0].description").value(templates.getFirst().description()))
            .andExpect(jsonPath("$.[0].categories.[0].name").value(templates.getFirst().categories().getFirst().name()))
            .andExpect(jsonPath("$.[0].categories.[0].position").value(templates.getFirst().categories().getFirst().position()))
            .andExpect(jsonPath("$.[0].categories.[0].lightBackgroundColor").value(templates.getFirst().categories().getFirst().lightBackgroundColor()))
            .andExpect(jsonPath("$.[0].categories.[0].lightTextColor").value(templates.getFirst().categories().getFirst().lightTextColor()))
            .andExpect(jsonPath("$.[0].categories.[0].darkBackgroundColor").value(templates.getFirst().categories().getFirst().darkBackgroundColor()))
            .andExpect(jsonPath("$.[0].categories.[0].darkTextColor").value(templates.getFirst().categories().getFirst().darkTextColor()));
    }

    @Test
    void getTemplates_WhenUserNotLoggedIn_Throws401() throws Exception {
        mockMvc.perform(get("/api/templates")
                        .with(anonymous())
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}