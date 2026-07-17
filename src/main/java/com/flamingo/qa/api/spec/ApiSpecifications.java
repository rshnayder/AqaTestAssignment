package com.flamingo.qa.api.spec;

import com.flamingo.qa.api.filter.RestAssuredLoggingFilter;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

public final class ApiSpecifications {

    private ApiSpecifications() {
    }

    public static RequestSpecification jsonSpec(String baseUri) {
        return new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .setContentType("application/json")
                .setAccept("application/json")
                .addFilter(new RestAssuredLoggingFilter())
                .build();
    }
}
