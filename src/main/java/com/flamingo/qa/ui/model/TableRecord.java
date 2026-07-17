package com.flamingo.qa.ui.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TableRecord {
    private String firstName;
    private String lastName;
    private String email;
    private int age;
    private int salary;
    private String department;
}
