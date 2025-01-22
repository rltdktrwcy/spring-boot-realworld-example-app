package io.spring.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.application.user.UpdateUserCommand;
import io.spring.application.user.UpdateUserParam;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CurrentUserApi.class)
@Disabled
public class CurrentUserApiTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserQueryService userQueryService;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserDetailsService userDetailsService;

    private User user;
    private UserData userData;
    private String token;

    @BeforeEach
    public void setUp() {
        user = new User("email@test.com", "username", "123", "bio", "image");
        userData = new UserData(user.getId(), user.getEmail(), user.getUsername(), user.getBio(), user.getImage());
        token = "Token jwt123";

        when(userQueryService.findById(user.getId())).thenReturn(Optional.of(userData));
    }

    @Test
    @WithMockUser(username = "username")
    public void should_get_current_user_success() throws Exception {
        mockMvc.perform(get("/user")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value(userData.getEmail()))
                .andExpect(jsonPath("$.user.username").value(userData.getUsername()))
                .andExpect(jsonPath("$.user.bio").value(userData.getBio()))
                .andExpect(jsonPath("$.user.image").value(userData.getImage()))
                .andExpect(jsonPath("$.user.token").value("jwt123"));
    }

    @Test
    @WithMockUser(username = "username")
    public void should_update_current_user_profile_success() throws Exception {
        String requestBody = "{\"user\":{\"email\":\"new@test.com\",\"username\":\"newname\",\"bio\":\"new bio\",\"image\":\"new image\"}}";

        UpdateUserParam updateUserParam = UpdateUserParam.builder()
            .email("new@test.com")
            .username("newname")
            .bio("new bio")
            .image("new image")
            .build();
        UpdateUserCommand updateUserCommand = new UpdateUserCommand(user, updateUserParam);

        Mockito.doNothing().when(userService).updateUser(updateUserCommand);

        mockMvc.perform(put("/user")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value(userData.getEmail()))
                .andExpect(jsonPath("$.user.username").value(userData.getUsername()))
                .andExpect(jsonPath("$.user.bio").value(userData.getBio()))
                .andExpect(jsonPath("$.user.image").value(userData.getImage()))
                .andExpect(jsonPath("$.user.token").value("jwt123"));
    }

    @Test
    public void should_get_401_when_not_authorized() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "username")
    public void should_get_422_with_invalid_update_param() throws Exception {
        String requestBody = "{\"user\":{\"email\":\"invalid email\"}}";

        mockMvc.perform(put("/user")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnprocessableEntity());
    }
}
