package com.kantar.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserVO extends BaseVO {
    private String user_id;
    private String user_pw;
    private String user_name;
    private String user_phone;
    private Integer user_type;
    private Integer user_status;
    private Integer role_type;
    private Integer exp;

    private String user_pw_origin; // 비밀번호 확인용
}