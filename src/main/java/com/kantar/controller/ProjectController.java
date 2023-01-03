package com.kantar.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kantar.base.BaseController;
import com.kantar.mapper.ProjectMapper;
import com.kantar.model.CommonResult;
import com.kantar.service.ResponseService;
import com.kantar.vo.ProjectVO;

@RestController("/api")
public class ProjectController extends BaseController {
    private ResponseService responseService;
    private ProjectMapper projectMapper;

    /**
     * 프로젝트 저장 - csv 저장 후 Job No, 프로젝트 이름 리턴
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/create")
    public CommonResult create(ProjectVO paramVo) throws Exception {
        try {
            ProjectVO rs = projectMapper.savProjectInfo(paramVo);
            return responseService.getSuccessResult(rs);
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("999","오류가 발생하였습니다.");
        }
    }
}
