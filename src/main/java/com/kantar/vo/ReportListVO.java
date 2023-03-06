package com.kantar.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportListVO extends BaseVO {
    private Integer idx_report;
    private String keyword;
    private String keytype;
    private Integer keycount;
    private Integer d_count;
    private Integer d_count_total;
    private String job_no;
    private String report_id;
    private String title;
    private String status_str;
}