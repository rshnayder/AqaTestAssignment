package com.flamingo.qa.graphql.model;

import lombok.Data;

import java.util.List;

@Data
public class MovieConnection {
    private List<MovieEdge> edges;
    private PageInfo pageInfo;
    private Aggregate aggregate;
}
