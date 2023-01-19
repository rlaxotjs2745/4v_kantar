package com.kantar.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectViewVO {
    String person;
    String chapter;
    String subchapter;
    String question;
    String answer;
}
