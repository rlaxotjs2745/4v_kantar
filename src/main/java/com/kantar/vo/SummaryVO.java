package com.kantar.vo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SummaryVO{
    private List<SumtextVO> text;
    private Map<String, Object> nlpConfig;
}