package com.flamingo.qa.graphql.model;

import lombok.Data;

@Data
public class Movie {
    private String id;
    private String title;
    private String slug;
    private String imdbId;
    private User publishedBy;
}
