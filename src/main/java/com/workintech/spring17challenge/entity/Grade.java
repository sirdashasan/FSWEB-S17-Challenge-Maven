package com.workintech.spring17challenge.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Grade {

    private Integer coefficient;
    private String note;
}
