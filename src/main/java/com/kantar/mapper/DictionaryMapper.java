package com.kantar.mapper;

import java.util.List;

import com.kantar.vo.DictionaryDataVO;
import com.kantar.vo.DictionaryVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DictionaryMapper {
    public List<DictionaryVO> getDictionaryList(DictionaryVO paramVo) throws Exception;

    public DictionaryVO getDictionary(DictionaryVO paramVo) throws Exception;

    public DictionaryDataVO getDictionaryData(DictionaryDataVO paramVo) throws Exception;

    public void deleteDictionary(DictionaryVO paramVo) throws Exception;

    public Integer insertDictionary(DictionaryVO paramVo) throws Exception;

    public Integer insertDictionaryData(DictionaryDataVO paramVo) throws Exception;

    public List<DictionaryDataVO> getDictionaryDataList(DictionaryVO paramVo) throws Exception;

    public void updateDictionaryData(DictionaryDataVO paramVo) throws Exception;

    public void deleteDictionaryData(DictionaryDataVO paramVo) throws Exception;
}
