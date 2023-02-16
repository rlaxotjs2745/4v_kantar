package com.kantar.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WordCloudVO extends BaseVO {
    private Integer idx_project;
    private Integer idx_project_job_projectid;
    private Integer idx_wordcloud;
    private Integer idx_word_filter;
    private String title;
    private String tp1;
    private String tp2;
    private String tp3;
    private String tp4;
    private String tp5;
}