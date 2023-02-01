package com.kantar.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.kantar.vo.ProjectVO;

@Mapper
public interface ReportMapper {
    public Integer getReportListCount(ProjectVO paramVo) throws Exception;

    public List<ProjectVO> getReportFileList(ProjectVO paramVo) throws Exception;

    public Integer getReportSeq() throws Exception;

    public Integer savReport(ProjectVO paramVo) throws Exception;

    public ProjectVO getReportIdx(ProjectVO paramVo) throws Exception;

    public Integer saveReportData(ProjectVO paramVo) throws Exception;

    public Integer modiReportData(ProjectVO paramVo) throws Exception;

    public ProjectVO savReportMake(ProjectVO paramVo) throws Exception;

    public ProjectVO getReportView(ProjectVO paramVo) throws Exception;

    public List<ProjectVO> getReportList(ProjectVO paramVo) throws Exception;

    public List<ProjectVO> getReportFilterList(ProjectVO paramVo) throws Exception;

    public List<ProjectVO> getReportFileListOne(ProjectVO paramVo) throws Exception;
}
