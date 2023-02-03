package com.kantar.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportFilterDataVO extends BaseVO {
    private Integer idx_project;
    private Integer idx_project_job_projectid;
    private Integer idx_report;
    private Integer idx_report_data;
    private Integer idx_filter;
    private Integer filter_op1; // 1: 전체요약 2:~챕터요약 3:~서브챕터요약 4:~질문요약 // 화자,사전키워드는 디폴트
    private Integer filter_op2; // 요약키워드 1:한글자포함 2:한글자제외
    private Integer filter_op3; // 요약키워드 1:명사 2:형용사
    private String report_name;
    private String tp1; // 화자
    private String tp2; // 챕터
    private String tp3; // 서브챕터
    private String tp4; // 질문
    private String tp5; // 사전키워드
}