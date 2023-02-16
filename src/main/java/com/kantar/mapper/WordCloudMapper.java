package com.kantar.mapper;

import com.kantar.vo.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface WordCloudMapper {

    public Integer createWordCloudFilter(FilterVO paramVo) throws Exception;

    public void delWordCloudFilter(FilterVO paramVo) throws Exception;

    public Integer createWordCloudFilterData(Map<String, Object> sptd) throws Exception;

    public int saveWordCloud(WordCloudVO wc) throws Exception;

    public int findWordCloudKeyword(ReportFilterKeywordVO reKeywords) throws Exception;

    public void createWordCloudKeywordData(ReportFilterKeywordVO reKeywords) throws Exception;

    public Integer getWordCloudListCount(WordCloudVO wc) throws Exception;

    public List<FilterVO> getWordCloudList(WordCloudVO wc) throws Exception;

    public List<WordCloudDataVO> getWordCloudDetail(WordCloudVO wc) throws Exception;

    public List<FilterVO> getWordCloudFilterDetail(WordCloudVO wc) throws Exception;
}
