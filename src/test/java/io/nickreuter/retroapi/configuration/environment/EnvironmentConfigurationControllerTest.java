package io.nickreuter.retroapi.configuration.environment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
@SpringBootTest(properties = {"web.authentication.authority=http://this.is.a/url", "web.authentication.client-id=clientId"})
class EnvironmentConfigurationControllerTest {
    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getConfiguration_ReturnsWebAuthenticationConfig() throws Exception {
        mockMvc.perform(get("/api/configuration"))
                .andExpect(jsonPath("$.webAuthentication.authority").value("http://this.is.a/url"))
                .andExpect(jsonPath("$.webAuthentication.clientId").value("clientId"));
    }
}