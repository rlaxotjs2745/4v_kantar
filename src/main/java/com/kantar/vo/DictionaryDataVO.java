package com.kantar.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DictionaryDataVO extends BaseVO {
    private Integer idx_dictionary_data;
    private Integer idx_dictionary;
    private String keyword;
    private String keyword01;
    private String keyword02;
    private String keyword03;
    private String keyword04;
    private String keyword05;
    private String keyword06;
    private String keyword07;
    private String keyword08;
    private String keyword09;
    private String keyword10;
}
