package com.flamingo.qa.graphql.model;

import lombok.Data;

@Data
public class MovieEdge {
    private Movie node;
    private String cursor;
}
