package com.kantar.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DictionaryVO extends BaseVO{
    private Integer idx_dictionary;
    private String title; //사전 이름
    private String filepath; // 파일 경로
    private String filename; // 파일 이름
    private Integer dic_type; // 사전 종류 0: 기본사전, 1: 사용자 사전
    private Integer dic_count; // 표제어 수
}
