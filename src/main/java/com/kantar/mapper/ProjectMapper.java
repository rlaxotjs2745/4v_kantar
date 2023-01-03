package com.kantar.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.kantar.vo.ProjectVO;

@Mapper
public interface ProjectMapper {
    public ProjectVO savProjectInfo(ProjectVO paramVo) throws Exception;
}
