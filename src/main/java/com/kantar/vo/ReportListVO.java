package com.kantar.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportListVO {
    private Integer idx_report;
    private String keyword;
    private String keytype;
    private Integer keycount;
    private Integer d_count;
    private Integer d_count_total;
    private String job_no;
    private String report_id;
    private String title;
    private String create_dt;
    private String status_str;
}