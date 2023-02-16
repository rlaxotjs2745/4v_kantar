package com.kantar.vo;

import lombok.Data;

import java.util.List;

@Data
public class DictionaryDownVO {
    private Integer idx_dictionary;
    private List<DictionaryDataVO> dictionaryData;
}
