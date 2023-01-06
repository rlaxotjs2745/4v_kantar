package com.kantar.vo;

import java.math.BigInteger;

import lombok.*;

@Getter
@Setter
public class BaseVO{
    private BigInteger idx_user;
    private String create_dt;
    private String update_dt;
    private Integer currentPage;
    private Integer recordCountPerPage;
    private Integer firstIndex;
    private String token;
}