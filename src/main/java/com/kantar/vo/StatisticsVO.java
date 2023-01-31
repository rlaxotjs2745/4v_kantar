package com.kantar.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class StatisticsVO extends BaseVO{
    //데이터 사용량
    private Integer idx_project;
    private Integer file_status; // 데이터 포함 여부. 1:사용 999:삭제
    private long file_cnt; // 첨부 파일 수
    private double file_size; // 프로젝트 파일 사이즈(MB)
    private long word_length; //프로젝트 파일 글자수
    private long report_cnt; // 프로젝트별 생성리포트수

    //API 사용량
    private Integer idx_report;
    private Integer api_type; // 1:요약 2:키워드
    private long summaryUsage; // 요약 사용량
    private long keywordUsage; // 키워드 사용량
    private String startDate;
    private String endDate;
}
