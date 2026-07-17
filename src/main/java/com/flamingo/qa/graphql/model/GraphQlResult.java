package com.flamingo.qa.graphql.model;

import lombok.Value;

@Value
public class GraphQlResult<T> {
    int statusCode;
    GraphQlResponse<T> body;
    String rawBody;
}
