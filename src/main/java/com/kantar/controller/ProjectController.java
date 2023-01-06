package com.kantar.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.kantar.base.BaseController;
import com.kantar.mapper.ProjectMapper;
import com.kantar.model.CommonResult;
import com.kantar.service.FileService;
import com.kantar.service.ProjectService;
import com.kantar.service.ResponseService;
import com.kantar.util.Excel;
import com.kantar.vo.ProjectVO;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/project")
public class ProjectController extends BaseController {
    private final ResponseService responseService;
    private final ProjectMapper projectMapper;
    private final FileService fileService;
    private final ProjectService projectService;
    private Excel excel;

    /**
     * 프로젝트 저장 - csv 저장 후 Job No, 프로젝트 이름 리턴
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
     @PostMapping("/create")
    public CommonResult create(MultipartHttpServletRequest req, ProjectVO paramVo) throws Exception {
        try {
            if(StringUtils.isEmpty(paramVo.getJob_no())){
                return responseService.getFailResult("project_create","JOB No를 입력해주세요.");
            }
            if(StringUtils.isEmpty(paramVo.getProject_name())){
                return responseService.getFailResult("project_create","프로젝트명을 입력해주세요.");
            }

            ProjectVO finfo = fileService.fileSave(req, paramVo.getJob_no(), "csv");
            paramVo.setFilename(finfo.getFilename());
            paramVo.setFilepath(finfo.getFilepath());

            ProjectVO rs = projectMapper.savProjectInfo(paramVo);
            if(rs == null){
                return responseService.getFailResult("project_create","저장을 할 수 없습니다.");
            }else{
                if(rs.getIdx_project()==0){
                    return responseService.getFailResult("project_create","저장에 실패하였습니다.");
                }else{
                    Map<String, Object> _rdata = new HashMap<String, Object>();
                    _rdata.put("idx_project",rs.getIdx_project());

                    ArrayList<Object> _elist = new ArrayList<Object>();
                    List<MultipartFile> fileList = req.getFiles("file");
                    if(fileList.size()>0){
                        for(MultipartFile mf : fileList) {
                            String fname = mf.getOriginalFilename();
                            List<Map<String, Object>> excelList = excel.getListData(mf, 1, 4);

                            Map<String, Object> _edata = new HashMap<String, Object>();
                            _edata.put("filename",fname);
                            _edata.put("datalist",excelList);
                            _elist.add(_edata);
                        }
                    }

                    _rdata.put("xlsdata",_elist);
                    return responseService.getSuccessResult(_rdata, "project_create", "프로젝트 설정이 저장되었습니다.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("project_create","오류가 발생하였습니다.");
        }
    }

    /**
     * 프로젝트 리스팅
     * @param req
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/list_project")
    public CommonResult getProjectList(HttpServletRequest req, ProjectVO paramVo) throws Exception {
        try {
            if(StringUtils.isEmpty(paramVo.getIdx_project()+"")){
                return responseService.getFailResult("list_project","프로젝트 INDEX가 없습니다.");
            }
            if(StringUtils.isEmpty(paramVo.getIdx_project_job_projectid()+"")){
                return responseService.getFailResult("list_project","프로젝트 INDEX가 없습니다.");
            }

            if(paramVo.getCurrentPage() != null){
                paramVo.setRecordCountPerPage(10);
                paramVo.setFirstIndex((paramVo.getCurrentPage()-1) * 10);
            }

            List<ProjectVO> rs = projectMapper.getProjectList(paramVo);
            if(rs!=null){
                return responseService.getSuccessResult(rs, "list_project", "프로젝트 리스팅 성공");
            }else{
                return responseService.getFailResult("list_project","프로젝트 리스트가 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("list_project","오류가 발생하였습니다.");
        }
    }

    /**
     * 프로젝트 생성
     * @param req
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/create_report")
    public CommonResult create_report(HttpServletRequest req, ProjectVO paramVo) throws Exception {
        try {
            if(StringUtils.isEmpty(paramVo.getIdx_project()+"")){
                return responseService.getFailResult("create_report","프로젝트 INDEX가 없습니다.");
            }
            if(StringUtils.isEmpty(paramVo.getIdx_project_job_projectid()+"")){
                return responseService.getFailResult("create_report","프로젝트 INDEX가 없습니다.");
            }

            projectService.create_report(req, paramVo);
            return responseService.getSuccessResult("create_report", "리포트 생성 시작.");
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("create_report","오류가 발생하였습니다.");
        }
    }

    /**
     * 리포트 리스팅
     * @param req
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/list_report")
    public CommonResult getReportList(HttpServletRequest req, ProjectVO paramVo) throws Exception {
        try {
            if(StringUtils.isEmpty(paramVo.getIdx_project()+"")){
                return responseService.getFailResult("create_report","프로젝트 INDEX가 없습니다.");
            }
            if(StringUtils.isEmpty(paramVo.getIdx_project_job_projectid()+"")){
                return responseService.getFailResult("create_report","프로젝트 INDEX가 없습니다.");
            }

            if(paramVo.getCurrentPage() != null){
                paramVo.setRecordCountPerPage(10);
                paramVo.setFirstIndex((paramVo.getCurrentPage()-1) * 10);
            }

            List<ProjectVO> rs = projectMapper.getReportList(paramVo);
            if(rs!=null){
                return responseService.getSuccessResult(rs, "create_report", "리포트 리스팅 성공");
            }else{
                return responseService.getFailResult("create_report","리포트 리스트가 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("create_report","오류가 발생하였습니다.");
        }
    }
}
