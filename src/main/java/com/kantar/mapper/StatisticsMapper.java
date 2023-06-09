package com.kantar.mapper;

import com.kantar.vo.*;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StatisticsMapper {
    public long getFileCount() throws Exception;

    public long getReportCount() throws Exception;

    void setProjectStatistics(StatisticsVO statisticsVO) throws Exception;

    int updateProjectReporteCnt(ProjectVO paramVo) throws Exception;

    int deleteProjectStatistics(ProjectVO paramVo) throws Exception;

    long getAllFileWordCnt() throws Exception;

    Double getAllFileSize() throws Exception;

    int getReportAPIUsage(StatisticsVO satisticsVO) throws Exception;

    void setSummaryAPIUsage(StatisticsVO statisticsVO) throws Exception;

    void setKeywordAPIUsage(StatisticsVO statisticsVO) throws Exception;

    ProjectVO getPjIdxToReport(StatisticsVO satisticsVO) throws Exception;

    void setSummaryAPIUsageAdd(StatisticsVO statisticsVO) throws Exception;

    void setKeywordAPIUsageAdd(StatisticsVO statisticsVO) throws Exception;

    long getApiStatisticsByUser() throws Exception;

    long getApiStatisticsByUser(StatisticsVO userInfo) throws Exception;

    double getApiDataByUser() throws Exception;

    StatisticsVO getApiDataByUser(StatisticsVO userInfo) throws Exception;

    long getKeywordStatisticsByUser() throws Exception;

    long getKeywordStatisticsByUser(StatisticsVO userInfo) throws Exception;
}
