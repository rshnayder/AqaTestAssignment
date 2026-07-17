package com.flamingo.qa.graphql;

import com.flamingo.qa.graphql.client.GraphQlClient;
import com.flamingo.qa.graphql.model.*;
import com.flamingo.qa.graphql.query.MovieQueries;
import com.flamingo.qa.support.RetryTestExtension;
import com.flamingo.qa.support.TestLoggingExtension;
import io.qameta.allure.Step;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Tag("graphql")
@ExtendWith({TestLoggingExtension.class, RetryTestExtension.class})
class HygraphMovieGraphQlTest {

    private static final String PUBLISHED = "PUBLISHED";

    private final GraphQlClient client = new GraphQlClient();

    @Test
    void shouldQueryMovieListWithPaginationLimit() {
        GraphQlResult<MoviesData> result = client.execute(GraphQlRequest.builder()
                .query(MovieQueries.MOVIES_WITH_LIMIT)
                .variables(Map.of("first", 2, "stage", PUBLISHED))
                .build(), MoviesData.class);

        assertSoftly(softly -> {
            softly.assertThat(result.getStatusCode()).isEqualTo(200);
            softly.assertThat(result.getBody().hasErrors()).isFalse();
            softly.assertThat(result.getBody().getData().getMovies())
                    .isNotEmpty()
                    .hasSizeLessThanOrEqualTo(2)
                    .allSatisfy(movie -> {
                        softly.assertThat(movie.getId()).isNotBlank();
                        softly.assertThat(movie.getTitle()).isNotBlank();
                        softly.assertThat(movie.getSlug()).isNotBlank();
                    });
        });
    }

    @Test
    void shouldQueryMoviesConnectionWithRelayPaginationMetadata() {
        GraphQlResult<MoviesConnectionData> result = client.execute(GraphQlRequest.builder()
                .query(MovieQueries.MOVIES_CONNECTION)
                .variables(Map.of("first", 2, "stage", PUBLISHED))
                .build(), MoviesConnectionData.class);

        var moviesConnection = result.getBody().getData().getMoviesConnection();
        assertSoftly(softly -> {
            softly.assertThat(result.getStatusCode()).isEqualTo(200);
            softly.assertThat(result.getBody().hasErrors()).isFalse();
            softly.assertThat(moviesConnection.getEdges()).hasSizeLessThanOrEqualTo(2);
            softly.assertThat(moviesConnection.getPageInfo().getPageSize()).isLessThanOrEqualTo(2);
            softly.assertThat(moviesConnection.getAggregate().getCount()).isPositive();
        });
    }

    @Test
    void shouldQuerySingleMovieById() {
        Movie firstMovie = firstPublishedMovie();

        GraphQlResult<MovieData> result = client.execute(GraphQlRequest.builder()
                .query("query { movie(where: { id: \"" + firstMovie.getId() + "\" }, stage: PUBLISHED) { id title slug imdbId } }")
                .build(), MovieData.class);

        Movie movie = result.getBody().getData().getMovie();
        assertSoftly(softly -> {
            softly.assertThat(result.getStatusCode()).isEqualTo(200);
            softly.assertThat(result.getBody().hasErrors()).isFalse();
            softly.assertThat(movie.getId()).isEqualTo(firstMovie.getId());
            softly.assertThat(movie.getTitle()).isEqualTo(firstMovie.getTitle());
        });
    }

    @Test
    void shouldQuerySingleMovieUsingGraphQlVariables() {
        Movie firstMovie = firstPublishedMovie();

        GraphQlResult<MovieData> result = client.execute(GraphQlRequest.builder()
                .query(MovieQueries.MOVIE_BY_ID)
                .variables(Map.of("id", firstMovie.getId(), "stage", PUBLISHED))
                .build(), MovieData.class);

        assertSoftly(softly -> {
            softly.assertThat(result.getStatusCode()).isEqualTo(200);
            softly.assertThat(result.getBody().hasErrors()).isFalse();
            softly.assertThat(result.getBody().getData().getMovie().getId()).isEqualTo(firstMovie.getId());
        });
    }

    @Test
    void shouldQueryMovieFragmentWithNestedPublisher() {
        GraphQlResult<MoviesData> result = client.execute(GraphQlRequest.builder()
                .query(MovieQueries.MOVIE_WITH_FRAGMENT_AND_NESTED_USER)
                .variables(Map.of("first", 2, "stage", PUBLISHED))
                .build(), MoviesData.class);

        assertSoftly(softly -> {
            softly.assertThat(result.getStatusCode()).isEqualTo(200);
            softly.assertThat(result.getBody().hasErrors()).isFalse();
            softly.assertThat(result.getBody().getData().getMovies())
                    .isNotEmpty()
                    .allSatisfy(movie -> softly.assertThat(movie.getPublishedBy().getName()).isNotBlank());
        });
    }

    @Test
    void shouldReturnNullDataForNonExistingMovieId() {
        GraphQlResult<MovieData> result = client.execute(GraphQlRequest.builder()
                .query(MovieQueries.MOVIE_BY_ID)
                .variables(Map.of("id", "does-not-exist", "stage", PUBLISHED))
                .build(), MovieData.class);

        assertSoftly(softly -> {
            softly.assertThat(result.getStatusCode()).isEqualTo(200);
            softly.assertThat(result.getBody().hasErrors()).isFalse();
            softly.assertThat(result.getBody().getData().getMovie()).isNull();
        });
    }

    @Test
    void shouldReturnErrorsForMalformedQuery() {
        GraphQlResult<MovieData> result = client.execute(GraphQlRequest.builder()
                .query(MovieQueries.MALFORMED_QUERY)
                .build(), MovieData.class);

        assertSoftly(softly -> {
            softly.assertThat(result.getStatusCode()).isEqualTo(400);
            softly.assertThat(result.getBody().getData()).isNull();
            softly.assertThat(result.getBody().getErrors())
                    .isNotEmpty()
                    .first()
                    .extracting(error -> error.getMessage())
                    .asString()
                    .containsIgnoringCase("Parse error");
        });
    }

    @Test
    void shouldReturnValidationErrorForNonExistentField() {
        GraphQlResult<MoviesData> result = client.execute(GraphQlRequest.builder()
                .query(MovieQueries.NON_EXISTENT_FIELD)
                .build(), MoviesData.class);

        assertSoftly(softly -> {
            softly.assertThat(result.getStatusCode()).isEqualTo(400);
            softly.assertThat(result.getBody().getData()).isNull();
            softly.assertThat(result.getBody().getErrors())
                    .isNotEmpty()
                    .first()
                    .extracting(error -> error.getMessage())
                    .asString()
                    .contains("fieldThatDoesNotExist");
        });
    }

    @Step("Get first published movie test precondition")
    private Movie firstPublishedMovie() {
        GraphQlResult<MoviesData> result = client.execute(GraphQlRequest.builder()
                .query(MovieQueries.MOVIES_WITH_LIMIT)
                .variables(Map.of("first", 1, "stage", PUBLISHED))
                .build(), MoviesData.class);
        List<Movie> movies = result.getBody().getData().getMovies();
        assertThat(movies).isNotEmpty();
        return movies.get(0);
    }
}
