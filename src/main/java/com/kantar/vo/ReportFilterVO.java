package com.kantar.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportFilterVO extends BaseVO {
    private Integer idx_report_filter;
    private String filter_title;
}