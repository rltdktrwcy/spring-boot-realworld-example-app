package io.spring.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.UpdateArticleParam;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.joda.time.DateTime;

@ExtendWith(MockitoExtension.class)
class ArticleApiTests {

    @Mock private ArticleQueryService articleQueryService;
    @Mock private ArticleRepository articleRepository;
    @Mock private ArticleCommandService articleCommandService;

    private ArticleApi articleApi;
    private Article article;
    private ArticleData articleData;
    private User user;

    @BeforeEach
    void setUp() {
        articleApi = new ArticleApi(articleQueryService, articleRepository, articleCommandService);

        user = new User("email@email.com", "username", "123", "", "");

        article = new Article(
            "Test Title",
            "Test Description",
            "Test Body",
            Arrays.asList("test"),
            user.getId(),
            new DateTime()
        );

        articleData = new ArticleData(
            article.getId(),
            article.getSlug(),
            article.getTitle(),
            article.getDescription(),
            article.getBody(),
            false,
            0,
            article.getCreatedAt(),
            article.getUpdatedAt(),
            Arrays.asList("test"),
            new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false)
        );
    }

    @Test
    void should_get_article_success() {
        String slug = "test-slug";
        when(articleQueryService.findBySlug(eq(slug), eq(user))).thenReturn(Optional.of(articleData));

        ResponseEntity<?> response = articleApi.article(slug, user);
        assert response.getStatusCode().is2xxSuccessful();
    }

    @Test
    void should_throw_not_found_when_article_not_exist() {
        String slug = "not-exist";
        when(articleQueryService.findBySlug(eq(slug), eq(user))).thenReturn(Optional.empty());

        try {
            articleApi.article(slug, user);
        } catch (ResourceNotFoundException e) {
            assert true;
            return;
        }
        assert false;
    }

    @Test
    void should_update_article_success() {
        String slug = "test-slug";
        UpdateArticleParam updateArticleParam = new UpdateArticleParam("new title", "new body", "new desc");

        when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));
        when(articleCommandService.updateArticle(eq(article), eq(updateArticleParam))).thenReturn(article);
        when(articleQueryService.findBySlug(eq(article.getSlug()), eq(user))).thenReturn(Optional.of(articleData));

        ResponseEntity<?> response = articleApi.updateArticle(slug, user, updateArticleParam);
        assert response.getStatusCode().is2xxSuccessful();
    }

    @Test
    void should_throw_not_found_when_update_not_exist_article() {
        String slug = "not-exist";
        UpdateArticleParam updateArticleParam = new UpdateArticleParam("title", "body", "desc");

        when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.empty());

        try {
            articleApi.updateArticle(slug, user, updateArticleParam);
        } catch (ResourceNotFoundException e) {
            assert true;
            return;
        }
        assert false;
    }

    @Test
    void should_throw_no_auth_when_not_author() {
        String slug = "test-slug";
        User anotherUser = new User("other@email.com", "other", "123", "", "");
        UpdateArticleParam updateArticleParam = new UpdateArticleParam("title", "body", "desc");

        when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));

        try {
            articleApi.updateArticle(slug, anotherUser, updateArticleParam);
        } catch (NoAuthorizationException e) {
            assert true;
            return;
        }
        assert false;
    }

    @Test
    void should_delete_article_success() {
        String slug = "test-slug";

        when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));

        ResponseEntity<?> response = articleApi.deleteArticle(slug, user);
        verify(articleRepository).remove(article);
        assert response.getStatusCode().is2xxSuccessful();
    }

    @Test
    void should_throw_not_found_when_delete_not_exist_article() {
        String slug = "not-exist";

        when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.empty());

        try {
            articleApi.deleteArticle(slug, user);
        } catch (ResourceNotFoundException e) {
            assert true;
            return;
        }
        assert false;
    }

    @Test
    void should_throw_no_auth_when_delete_not_author_article() {
        String slug = "test-slug";
        User anotherUser = new User("other@email.com", "other", "123", "", "");

        when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));

        try {
            articleApi.deleteArticle(slug, anotherUser);
        } catch (NoAuthorizationException e) {
            assert true;
            return;
        }
        assert false;
    }
}
