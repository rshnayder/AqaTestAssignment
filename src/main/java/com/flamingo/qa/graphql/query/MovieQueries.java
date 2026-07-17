package com.flamingo.qa.graphql.query;

public final class MovieQueries {

    public static final String MOVIES_WITH_LIMIT =
            "query MoviesWithLimit($first: Int!, $stage: Stage!) { "
                    + "movies(first: $first, stage: $stage) { id title slug imdbId } "
                    + "}";

    public static final String MOVIES_CONNECTION =
            "query MoviesConnection($first: Int!, $stage: Stage!) { "
                    + "moviesConnection(first: $first, stage: $stage) { "
                    + "edges { cursor node { id title slug } } "
                    + "pageInfo { hasNextPage hasPreviousPage pageSize startCursor endCursor } "
                    + "aggregate { count } "
                    + "} "
                    + "}";

    public static final String MOVIE_BY_ID =
            "query MovieById($id: ID!, $stage: Stage!) { "
                    + "movie(where: { id: $id }, stage: $stage) { id title slug imdbId } "
                    + "}";

    public static final String MOVIE_WITH_FRAGMENT_AND_NESTED_USER =
            "query MoviesWithPublisher($first: Int!, $stage: Stage!) { "
                    + "movies(first: $first, stage: $stage) { "
                    + "...MovieCard "
                    + "publishedBy { id name isActive kind } "
                    + "} "
                    + "} "
                    + "fragment MovieCard on Movie { id title slug imdbId }";

    public static final String MALFORMED_QUERY =
            "query { movies(first: 1) { id title ";

    public static final String NON_EXISTENT_FIELD =
            "query { movies(first: 1, stage: PUBLISHED) { id fieldThatDoesNotExist } }";

    private MovieQueries() {
    }
}
