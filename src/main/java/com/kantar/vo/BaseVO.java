package com.kantar.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
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