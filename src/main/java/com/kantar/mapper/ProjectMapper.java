package com.kantar.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.kantar.vo.ProjectVO;

@Mapper
public interface ProjectMapper {
    public ProjectVO getProjectInfo(ProjectVO paramVo) throws Exception;

    public List<ProjectVO> getProjectList(ProjectVO paramVo) throws Exception;

    public ProjectVO savProjectInfo(ProjectVO paramVo) throws Exception;

    public Integer comProject(ProjectVO paramVo) throws Exception;

    public ProjectVO savReportMake(ProjectVO paramVo) throws Exception;

    public List<ProjectVO> getReportList(ProjectVO paramVo) throws Exception;
}
