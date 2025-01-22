package io.spring.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.UpdateArticleParam;
import io.spring.application.data.ArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ArticleApiTests {

    @Mock
    private ArticleQueryService articleQueryService;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleCommandService articleCommandService;

    private ArticleApi articleApi;
    private Article article;
    private ArticleData articleData;
    private User user;

    @BeforeEach
    void setUp() {
        articleApi = new ArticleApi(articleQueryService, articleRepository, articleCommandService);

        user = new User("test@test.com", "testuser", "password", "bio", "image");

        article = new Article("Test Title", "Test Description", "Test Body", null, user.getId());
        article.update("Test Title", "Test Description", "Test Body");

        articleData = new ArticleData();
        articleData.setId("articleId");
        articleData.setSlug("test-slug");
        articleData.setTitle("Test Title");
        articleData.setDescription("Test Description");
        articleData.setBody("Test Body");
    }

    @Test
    @Disabled
    void should_get_article_success() {
        String slug = "test-slug";
        when(articleQueryService.findBySlug(eq(slug), eq(user))).thenReturn(Optional.of(articleData));

        ResponseEntity<?> response = articleApi.article(slug, user);

        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    @Disabled
    void should_throw_not_found_when_article_not_exist() {
        String slug = "not-exists";
        when(articleQueryService.findBySlug(eq(slug), eq(user))).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            articleApi.article(slug, user);
        });
    }

    @Test
    @Disabled
    void should_update_article_success() {
        String slug = "test-slug";
        UpdateArticleParam updateArticleParam = new UpdateArticleParam("New Title", "New Body", "New Description");

        try (MockedStatic<AuthorizationService> mockedStatic = Mockito.mockStatic(AuthorizationService.class)) {
            mockedStatic.when(() -> AuthorizationService.canWriteArticle(eq(user), eq(article))).thenReturn(true);

            when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));
            when(articleCommandService.updateArticle(eq(article), eq(updateArticleParam))).thenReturn(article);
            when(articleQueryService.findBySlug(eq(article.getSlug()), eq(user))).thenReturn(Optional.of(articleData));

            ResponseEntity<?> response = articleApi.updateArticle(slug, user, updateArticleParam);

            assert response.getStatusCode() == HttpStatus.OK;
            verify(articleCommandService).updateArticle(eq(article), eq(updateArticleParam));
        }
    }

    @Test
    @Disabled
    void should_throw_not_found_when_update_article_not_exist() {
        String slug = "not-exist";
        UpdateArticleParam updateArticleParam = new UpdateArticleParam("title", "body", "description");

        when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            articleApi.updateArticle(slug, user, updateArticleParam);
        });
    }

    @Test
    @Disabled
    void should_throw_error_when_not_author() {
        String slug = "test-slug";
        UpdateArticleParam updateArticleParam = new UpdateArticleParam("title", "body", "description");
        User otherUser = new User("other@test.com", "other", "password", "bio", "image");

        try (MockedStatic<AuthorizationService> mockedStatic = Mockito.mockStatic(AuthorizationService.class)) {
            mockedStatic.when(() -> AuthorizationService.canWriteArticle(eq(otherUser), eq(article))).thenReturn(false);

            when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));

            org.junit.jupiter.api.Assertions.assertThrows(NoAuthorizationException.class, () -> {
                articleApi.updateArticle(slug, otherUser, updateArticleParam);
            });
        }
    }

    @Test
    @Disabled
    void should_delete_article_success() {
        String slug = "test-slug";

        try (MockedStatic<AuthorizationService> mockedStatic = Mockito.mockStatic(AuthorizationService.class)) {
            mockedStatic.when(() -> AuthorizationService.canWriteArticle(eq(user), eq(article))).thenReturn(true);

            when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));

            ResponseEntity<?> response = articleApi.deleteArticle(slug, user);

            assert response.getStatusCode() == HttpStatus.NO_CONTENT;
            verify(articleRepository).remove(eq(article));
        }
    }

    @Test
    @Disabled
    void should_throw_not_found_when_delete_article_not_exist() {
        String slug = "not-exist";

        when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            articleApi.deleteArticle(slug, user);
        });
    }

    @Test
    @Disabled
    void should_throw_error_when_delete_not_author() {
        String slug = "test-slug";
        User otherUser = new User("other@test.com", "other", "password", "bio", "image");

        try (MockedStatic<AuthorizationService> mockedStatic = Mockito.mockStatic(AuthorizationService.class)) {
            mockedStatic.when(() -> AuthorizationService.canWriteArticle(eq(otherUser), eq(article))).thenReturn(false);

            when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));

            org.junit.jupiter.api.Assertions.assertThrows(NoAuthorizationException.class, () -> {
                articleApi.deleteArticle(slug, otherUser);
            });
        }
    }
}
