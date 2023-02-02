package com.kantar.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportFilterKeywordVO extends BaseVO {
    private int idx_report;
    private int idx_report_data;
    private String sum_keyword; // 요약추출 키워드
    private String[] dic_keywords; // 사전필터 키워드
    private String[] speaker;
    private Integer keycount;
    private Integer keytype; // 1:명사 2:형용사
}