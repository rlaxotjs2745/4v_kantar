package com.kantar.vo;

import lombok.*;

@Getter
@Setter
public class FilterDataVO {
    private Integer idx_filter_data;
    private Integer idx_filter;
    private Integer filter_type;
    private String filter_data;
}