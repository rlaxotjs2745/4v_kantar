package com.kantar.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportMetaDataVO extends BaseVO {
    private Integer idx_report;
    private String speaker;
    private String chapter;
    private int cnt;
    private int length;
}