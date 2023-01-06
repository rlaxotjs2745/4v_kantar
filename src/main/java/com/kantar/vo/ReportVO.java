package com.kantar.vo;

import lombok.*;

@Getter
@Setter
public class ReportVO extends BaseVO {
    private Integer idx_report_keyword;
    private Integer idx_report;
    private String keyword;
    private String keytype;
    private Integer keycount;
}