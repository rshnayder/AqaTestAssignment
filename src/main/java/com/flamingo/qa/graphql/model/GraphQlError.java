package com.flamingo.qa.graphql.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GraphQlError {
    private String message;
    private List<GraphQlLocation> locations;
    private List<Object> path;
    private Map<String, Object> extensions;
}
