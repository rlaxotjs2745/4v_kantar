package com.kantar.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatisticsVO extends BaseVO{
    private Integer idx_project;
    private Integer file_status; // 데이터 포함 여부. 1:사용 999:삭제
    private long file_cnt; // 첨부 파일 수
    private double file_size; // 프로젝트 파일 사이즈(MB)
    private long word_length; //프로젝트 파일 글자수
    private long report_cnt;
}
