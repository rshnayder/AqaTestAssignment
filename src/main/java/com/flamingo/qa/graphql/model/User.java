package com.flamingo.qa.graphql.model;

import lombok.Data;

@Data
public class User {
    private String id;
    private String name;
    private Boolean isActive;
    private String kind;
}
