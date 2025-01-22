package io.spring.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProfileApi.class)
@Disabled
class ProfileApiTests {

  @Autowired private MockMvc mockMvc;

  @MockBean private ProfileQueryService profileQueryService;

  @MockBean private UserRepository userRepository;

  private User user;
  private ProfileData profileData;
  private User currentUser;

  @BeforeEach
  void setUp() {
    user = new User("user@test.com", "username", "123", "bio", "image");
    currentUser = new User("current@test.com", "currentuser", "123", "bio", "image");
    profileData = new ProfileData("123", user.getUsername(), user.getBio(), user.getImage(), false);
  }

  @Test
  @WithMockUser
  @Disabled
  void should_get_profile_success() throws Exception {
    when(profileQueryService.findByUsername(eq(user.getUsername()), any()))
        .thenReturn(Optional.of(profileData));

    mockMvc
        .perform(get("/profiles/{username}", user.getUsername()))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  @Disabled
  void should_404_if_user_not_found() throws Exception {
    when(profileQueryService.findByUsername(eq(user.getUsername()), any()))
        .thenReturn(Optional.empty());

    mockMvc
        .perform(get("/profiles/{username}", user.getUsername()))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  @Disabled
  void should_follow_user_success() throws Exception {
    when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
    when(profileQueryService.findByUsername(eq(user.getUsername()), any()))
        .thenReturn(Optional.of(profileData));

    mockMvc
        .perform(post("/profiles/{username}/follow", user.getUsername()))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  @Disabled
  void should_404_if_user_not_found_when_follow() throws Exception {
    when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());

    mockMvc
        .perform(post("/profiles/{username}/follow", user.getUsername()))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  @Disabled
  void should_unfollow_user_success() throws Exception {
    FollowRelation followRelation = new FollowRelation(currentUser.getId(), user.getId());

    when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
    when(userRepository.findRelation(any(), any())).thenReturn(Optional.of(followRelation));
    when(profileQueryService.findByUsername(eq(user.getUsername()), any()))
        .thenReturn(Optional.of(profileData));

    mockMvc
        .perform(delete("/profiles/{username}/follow", user.getUsername()))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  @Disabled
  void should_404_if_user_not_found_when_unfollow() throws Exception {
    when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());

    mockMvc
        .perform(delete("/profiles/{username}/follow", user.getUsername()))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  @Disabled
  void should_404_if_follow_not_found_when_unfollow() throws Exception {
    when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
    when(userRepository.findRelation(any(), any())).thenReturn(Optional.empty());

    mockMvc
        .perform(delete("/profiles/{username}/follow", user.getUsername()))
        .andExpect(status().isNotFound());
  }
}
