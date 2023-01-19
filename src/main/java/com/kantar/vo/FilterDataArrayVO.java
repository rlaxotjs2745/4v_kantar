package com.kantar.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterDataArrayVO {
    private String filter_data;
}