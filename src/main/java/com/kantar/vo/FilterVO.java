package com.kantar.vo;

import java.util.List;

import lombok.*;

@Getter
@Setter
public class FilterVO {
    private Integer idx_project;
    private Integer idx_filter;
    private String filter_title;
    private String filter_tp;
    private Integer filter_type;
    private String filter_data;
    private String create_dt;
    private Integer idx_user;
    private String tp1;
    private String tp2;
    private String tp3;
    private String tp4;
    List<FilterDataVO> filterDataList;
}