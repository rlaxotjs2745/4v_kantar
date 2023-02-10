package com.kantar.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.kantar.vo.ProjectListVO;
import com.kantar.vo.ProjectVO;

@Mapper
public interface ProjectMapper {
    public ProjectVO getProjectInfo(ProjectVO paramVo) throws Exception;

    public List<ProjectListVO> getProjectList(ProjectVO paramVo) throws Exception;

    public Integer getProjectListCount(ProjectVO paramVo) throws Exception;

    public ProjectVO getProjectJobNo(ProjectVO paramVo) throws Exception;

    public Integer savProjectJobNo(ProjectVO paramVo) throws Exception;

    public Integer getProjectSeq(ProjectVO paramVo) throws Exception;

    public Integer savProjectJobProjectid(ProjectVO paramVo) throws Exception;

    public Integer modProjectJobProjectid(ProjectVO paramVo) throws Exception;

    public ProjectVO getProjectJobProjectid(ProjectVO paramVo) throws Exception;

    public Integer savProjectInfo(ProjectVO paramVo) throws Exception;

    public Integer modiProjectInfo(ProjectVO paramVo) throws Exception;

    public ProjectVO getProjectView(ProjectVO paramVo) throws Exception;

    public Integer comProject(ProjectVO paramVo) throws Exception;

    Integer savMergeProject(ProjectVO paramVo) throws Exception;

    ProjectVO getProjectInfoByIdx(ProjectVO paramVo) throws Exception;

    public ProjectVO getProjectDown(ProjectVO paramVo) throws Exception;

    public Integer savProjectJobProjectidAndFileInfo(ProjectVO paramVo) throws Exception;

    public ProjectVO getProjectInfoByProJobIdx(ProjectVO paramVo) throws Exception;
}
