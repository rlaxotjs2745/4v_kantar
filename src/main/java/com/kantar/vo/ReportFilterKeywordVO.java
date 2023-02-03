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
    private Integer keycount; // 요약추출 수
    private String[] dic_keywords; // (필터링용) 사전필터 키워드
    private String[] speaker; // (필터링용) 화자
    private Integer keytype; // 1:명사 2:형용사
    private Integer dic_yn; // 사전 포함 여부
}