package com.flamingo.qa.graphql.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphQlRequest {
    private String query;

    @Builder.Default
    private Map<String, Object> variables = Collections.emptyMap();
}
