package com.kantar.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterDataVO {
    private Integer idx_filter_data;
    private Integer idx_filter;
    private Integer filter_type;
    private String filter_data;
}