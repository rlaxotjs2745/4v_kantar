package com.kantar.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseVO{
    private Integer idx;
    private Integer idx_user;
    private String create_dt;
    private String update_dt;
    private Integer currentPage;
    private Integer recordCountPerPage;
    private Integer firstIndex;
    private String token;
    private Integer filter;
}