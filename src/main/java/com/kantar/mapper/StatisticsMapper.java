package com.kantar.mapper;

import com.kantar.vo.DictionaryDataVO;
import com.kantar.vo.DictionaryVO;
import com.kantar.vo.ProjectVO;
import com.kantar.vo.StatisticsVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StatisticsMapper {
    public long getFileCount() throws Exception;

    public long getReportCount() throws Exception;

    void setProjectStatistics(StatisticsVO statisticsVO) throws Exception;

    int updateProjectReporteCnt(ProjectVO paramVo) throws Exception;

    int deleteProjectStatistics(ProjectVO paramVo) throws Exception;

    long getAllFileWordCnt() throws Exception;

    Double getAllFileSize() throws Exception;
}
