package com.kantar.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportVO extends BaseVO {
    private Integer idx_report_keyword;
    private Integer idx_report;
    private Integer idx_project;
    private Integer idx_project_job_projectid;
    private String keyword;
    private String keytype;
    private Integer keycount;

    private String title;
    private String rfil0;   // 전체 요약 포함 여부 - 0:미포함,1:포함
    private String rfil1;   // 챕터별 요약 포함 여부 - 0:미포함,1:포함
    private String rfil2;   // 서브 챕터별 요약 포함 여부 - 0:미포함,1:포함
    private String rfil3;   // 질문별 요약 포함 여부 - 0:미포함,1:포함
    private String rfil4;   // 키워드 추출 시 한글자 제외 여부 - 0:미제외,1:제외
    private String rfil5;   // 키워드 품사 형태 - 0:선택없음,1:명사,2:형용사,3:명사+형용사

    private Integer idx_data; // 수정 요약문 인덱스
    private String summary_md; // 수정 요약문
}