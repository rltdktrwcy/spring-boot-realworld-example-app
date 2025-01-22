package io.spring.api;

import io.spring.application.TagsQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class TagsApiTests {

    @Mock
    private TagsQueryService tagsQueryService;

    @InjectMocks
    private TagsApi tagsApi;

    @Test
    void should_get_all_tags() {
        List<String> expectedTags = Arrays.asList("java", "spring", "test");
        when(tagsQueryService.allTags()).thenReturn(expectedTags);

        ResponseEntity response = tagsApi.getTags();

        assertEquals(200, response.getStatusCodeValue());

        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue(responseBody.containsKey("tags"));
        assertEquals(expectedTags, responseBody.get("tags"));
    }
}
