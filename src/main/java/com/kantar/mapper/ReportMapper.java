package com.kantar.mapper;

import java.util.List;

import com.kantar.vo.ReportFilterKeywordVO;
import com.kantar.vo.ReportMetaDataVO;
import com.kantar.vo.ReportVO;
import org.apache.ibatis.annotations.Mapper;

import com.kantar.vo.ReportListVO;
import com.kantar.vo.ProjectVO;
import com.kantar.vo.ProjectListVO;

@Mapper
public interface ReportMapper {
    public Integer getReportListCount(ProjectVO paramVo) throws Exception;

    public List<ProjectVO> getReportFileList(ProjectVO paramVo) throws Exception;

    public Integer getReportSeq() throws Exception;

    public Integer savReport(ProjectVO paramVo) throws Exception;

    public ProjectVO getReportIdx(ProjectVO paramVo) throws Exception;

    public List<ReportListVO> getReportListIdx(ProjectListVO paramVo) throws Exception;

    public Integer saveReportData(ProjectVO paramVo) throws Exception;

    public Integer modiReportData(ProjectVO paramVo) throws Exception;

    public ProjectVO savReportMake(ProjectVO paramVo) throws Exception;

    public ProjectVO getReportView(ProjectVO paramVo) throws Exception;

    public List<ProjectVO> getReportList(ProjectVO paramVo) throws Exception;

    public List<ProjectVO> getReportFilterList(ProjectVO paramVo) throws Exception;

    public List<ProjectVO> getReportFileListOne(ProjectVO paramVo) throws Exception;

    public void createReportFilterData(ReportFilterKeywordVO reKeywords) throws Exception;

    public Integer findReportKeyword(ReportFilterKeywordVO reKeywords) throws Exception;

    List<ProjectVO> getReportDataViewAll(ProjectVO paramVo) throws Exception;

    List<ReportFilterKeywordVO> getReportKeywordView(ProjectVO paramVo) throws Exception;

    public int getKeywordFindDictionary(ProjectVO paramVo) throws Exception;

    public int getMetadataInfoByIdx(ReportMetaDataVO md) throws Exception;

    public void insertMetadata(ReportMetaDataVO md) throws Exception;

    public void updateMetadataCnt(ReportMetaDataVO md) throws Exception;

    public List<ReportMetaDataVO> getMetadataInfoSpeaker(Integer idx) throws Exception;

    public List<ReportMetaDataVO> getMetadataInfoChapter(Integer idx) throws Exception;

    public int updateReportSummary(ProjectVO paramVo) throws Exception;

    public int chkReportAuth(ProjectVO paramVo) throws Exception;

    public int chkReportDataAuth(ProjectVO paramVo) throws Exception;

    public Integer updReportCountUp(ProjectVO paramVo) throws Exception;

    public void updateReportInfo(ProjectVO paramVo) throws Exception;

    public void updateReportSummary(ReportVO paramVo) throws Exception;
}
