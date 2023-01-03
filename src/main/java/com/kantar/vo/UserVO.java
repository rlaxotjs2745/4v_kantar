package com.kantar.vo;

import lombok.*;

@Getter
@Setter
public class UserVO extends BaseVO {
    private String user_id;
    private String user_pw;
    private Integer user_type;
    private Integer user_status;
}