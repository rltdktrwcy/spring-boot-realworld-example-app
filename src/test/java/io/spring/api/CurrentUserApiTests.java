package io.spring.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.application.user.UpdateUserParam;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CurrentUserApi.class)
@Disabled
class CurrentUserApiTests {

  @Autowired private MockMvc mockMvc;

  @MockBean private UserQueryService userQueryService;
  @MockBean private UserService userService;

  private User user;
  private UserData userData;

  @BeforeEach
  void setUp() {
    user = new User("email@test.com", "username", "123", "bio", "image");
    userData = new UserData(user.getId(), user.getEmail(), user.getUsername(), user.getBio(), user.getImage());
  }

  @Test
  @WithMockUser
  void should_get_current_user_profile() throws Exception {
    when(userQueryService.findById(any())).thenReturn(Optional.of(userData));

    mockMvc
        .perform(
            get("/user")
                .header("Authorization", "Token jwt123")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void should_update_current_user_profile() throws Exception {
    when(userQueryService.findById(any())).thenReturn(Optional.of(userData));

    String requestBody =
        "{\"email\":\"new@test.com\",\"username\":\"newname\",\"password\":\"newpass\",\"bio\":\"new bio\",\"image\":\"new image\"}";

    mockMvc
        .perform(
            put("/user")
                .header("Authorization", "Token jwt123")
                .content("{\"user\":" + requestBody + "}")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void should_get_401_without_token() throws Exception {
    mockMvc
        .perform(get("/user").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void should_get_401_with_invalid_token() throws Exception {
    mockMvc
        .perform(
            get("/user")
                .header("Authorization", "invalid_token")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void should_get_422_with_invalid_update_param() throws Exception {
    String requestBody = "{\"email\":\"invalid email\"}";

    mockMvc
        .perform(
            put("/user")
                .header("Authorization", "Token jwt123")
                .content("{\"user\":" + requestBody + "}")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnprocessableEntity());
  }
}
