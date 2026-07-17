package com.flamingo.qa.graphql.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GraphQlResponse<T> {
    private T data;
    private List<GraphQlError> errors;
    private Map<String, Object> extensions;

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
}
