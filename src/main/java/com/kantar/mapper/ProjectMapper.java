package com.kantar.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.kantar.vo.ProjectVO;

@Mapper
public interface ProjectMapper {
    public ProjectVO getProjectInfo(ProjectVO paramVo) throws Exception;

    public List<ProjectVO> getProjectList(ProjectVO paramVo) throws Exception;

    public ProjectVO savProjectInfo(ProjectVO paramVo) throws Exception;

    public ProjectVO getProjectJobNo(ProjectVO paramVo) throws Exception;

    public Integer savProjectJobNo(ProjectVO paramVo) throws Exception;

    public Integer getProjectSeq(ProjectVO paramVo) throws Exception;

    public Integer savProjectJobProjectid(ProjectVO paramVo) throws Exception;

    public Integer savProjectInfo2(ProjectVO paramVo) throws Exception;

    public ProjectVO getProjectView(ProjectVO paramVo) throws Exception;

    public Integer comProject(ProjectVO paramVo) throws Exception;

    public List<ProjectVO> getReportFileList(ProjectVO paramVo) throws Exception;

    public Integer savReport(ProjectVO paramVo) throws Exception;

    public ProjectVO getReportIdx(ProjectVO paramVo) throws Exception;

    public Integer savReportIdx(ProjectVO paramVo) throws Exception;

    public Integer saveReportData(ProjectVO paramVo) throws Exception;

    public ProjectVO savReportMake(ProjectVO paramVo) throws Exception;

    public ProjectVO getReportView(ProjectVO paramVo) throws Exception;

    public List<ProjectVO> getReportList(ProjectVO paramVo) throws Exception;

    public List<ProjectVO> getReportFilterList(ProjectVO paramVo) throws Exception;
}
