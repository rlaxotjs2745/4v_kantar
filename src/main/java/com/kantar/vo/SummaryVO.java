package com.kantar.vo;

import java.util.List;
import java.util.Map;

import lombok.*;

@Getter
@Setter
public class SummaryVO{
    private List<SumtextVO> text;
    private Map<String, Object> nlpConfig;
}