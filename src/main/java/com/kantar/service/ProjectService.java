package com.kantar.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kantar.mapper.ProjectMapper;
import com.kantar.vo.ProjectVO;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ProjectService {
    private ProjectMapper projectMapper;

    @Async
    @Transactional
    public void create_report(HttpServletRequest req, ProjectVO paramVo) throws Exception{
        try {
            projectMapper.savReportMake(paramVo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
