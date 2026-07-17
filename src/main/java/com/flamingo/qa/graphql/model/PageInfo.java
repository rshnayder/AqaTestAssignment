package com.flamingo.qa.graphql.model;

import lombok.Data;

@Data
public class PageInfo {
    private Boolean hasNextPage;
    private Boolean hasPreviousPage;
    private String startCursor;
    private String endCursor;
    private Integer pageSize;
}
