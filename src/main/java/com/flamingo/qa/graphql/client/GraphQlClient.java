package com.flamingo.qa.graphql.client;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flamingo.qa.api.spec.ApiSpecifications;
import com.flamingo.qa.config.FrameworkConfig;
import com.flamingo.qa.core.jackson.ObjectMapperFactory;
import com.flamingo.qa.graphql.model.GraphQlRequest;
import com.flamingo.qa.graphql.model.GraphQlResponse;
import com.flamingo.qa.graphql.model.GraphQlResult;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static io.restassured.RestAssured.given;

public class GraphQlClient {

    private static final int QUERY_SUMMARY_LIMIT = 180;

    private final RequestSpecification spec;
    private final ObjectMapper mapper;

    public GraphQlClient() {
        this(FrameworkConfig.getRequired("graphql.endpoint"));
    }

    public GraphQlClient(String endpoint) {
        this.spec = ApiSpecifications.jsonSpec(endpoint);
        this.mapper = ObjectMapperFactory.mapper();
    }

    public Response executeRaw(GraphQlRequest request) {
        String token = FrameworkConfig.get("graphql.token");
        RequestSpecification requestSpec = given().spec(spec);
        if (!token.isEmpty()) {
            requestSpec.header("Authorization", "Bearer " + token);
        }
        stepExecuteGraphQl(summarizeQuery(request.getQuery()), variableKeys(request), !token.isEmpty());
        return requestSpec
                .body(request)
                .post();
    }

    public <T> GraphQlResult<T> execute(GraphQlRequest request, Class<T> dataType) {
        Response response = executeRaw(request);
        String rawBody = response.getBody().asString();
        return new GraphQlResult<>(response.statusCode(), parse(rawBody, dataType), rawBody);
    }

    private <T> GraphQlResponse<T> parse(String rawBody, Class<T> dataType) {
        JavaType responseType = mapper.getTypeFactory()
                .constructParametricType(GraphQlResponse.class, dataType);
        try {
            return mapper.readValue(rawBody, responseType);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to parse GraphQL response: " + rawBody, exception);
        }
    }

    private String summarizeQuery(String query) {
        if (query == null) {
            return "<empty>";
        }
        String summary = query.replaceAll("\\s+", " ").trim();
        if (summary.length() <= QUERY_SUMMARY_LIMIT) {
            return summary;
        }
        return summary.substring(0, QUERY_SUMMARY_LIMIT) + "...";
    }

    private Set<String> variableKeys(GraphQlRequest request) {
        Map<String, Object> variables = request.getVariables();
        if (variables == null || variables.isEmpty()) {
            return Collections.emptySet();
        }
        return new TreeSet<>(variables.keySet());
    }

    @Step("Execute GraphQL operation: {0}, variables={1}, tokenPresent={2}")
    private void stepExecuteGraphQl(String operation, Set<String> variableKeys, boolean tokenPresent) {
    }
}
