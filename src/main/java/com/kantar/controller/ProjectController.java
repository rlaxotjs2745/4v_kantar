package com.kantar.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.apache.commons.io.FilenameUtils;

import com.kantar.base.BaseController;
import com.kantar.mapper.ProjectMapper;
import com.kantar.model.CommonResult;
import com.kantar.service.ProjectService;
import com.kantar.service.ResponseService;
import com.kantar.vo.ProjectVO;
import com.kantar.vo.ProjectViewVO;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/project")
public class ProjectController extends BaseController {
    @Autowired
    private ResponseService responseService;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Value("${file.upload-dir}")
    public String filepath;

    /**
     * 프로젝트 저장 - csv 저장 후 Job No, 프로젝트 이름 리턴
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/create")
    @Transactional
    public CommonResult create(MultipartHttpServletRequest req, ProjectVO paramVo) throws Exception {
        try {
            if(StringUtils.isEmpty(paramVo.getJob_no())){
                return responseService.getFailResult("project_create","JOB No를 입력해주세요.");
            }
            if(StringUtils.isEmpty(paramVo.getProject_name())){
                return responseService.getFailResult("project_create","프로젝트 이름을 입력해주세요.");
            }
            List<MultipartFile> fileList = req.getFiles("file");
            if(req.getFiles("file").get(0).getSize() != 0){
                fileList = req.getFiles("file");
            }
            if(fileList.size()>0){
                for(MultipartFile mf : fileList) {
                    if(mf.getSize()>0){
                        String fname = mf.getOriginalFilename();
                        String ext = FilenameUtils.getExtension(fname);
                        String contentType = mf.getContentType();
                        if (!ext.equals("csv")) {
                            return responseService.getFailResult("project_create",".csv 포맷 파일이 맞는지 확인 후 다시 업로드를 시도해주세요.");
                        }
                        if(!contentType.equals("text/csv")) {
                            return responseService.getFailResult("project_create",".csv 포맷 파일이 맞는지 확인 후 다시 업로드를 시도해주세요.");
                        }
                    }
                }

                String path = "/report/" + paramVo.getJob_no() + "/";
                String fullpath = this.filepath + path;
                File fileDir = new File(fullpath);
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }

                ArrayList<Object> _elist = new ArrayList<Object>();
                Map<String, Object> _rdata = new HashMap<String, Object>();
                for(MultipartFile mf : fileList) {
                    String originFileName = mf.getOriginalFilename();   // 원본 파일 명
                    mf.transferTo(new File(fullpath, originFileName));

                    Integer Jobno2 = 0;
                    ProjectVO Jobno = projectMapper.getProjectJobNo(paramVo);
                    if(Jobno==null){
                        Jobno2 = projectMapper.savProjectJobNo(paramVo);
                    }else{
                        paramVo.setIdx_project_job(Jobno.getIdx_project_job());
                        Jobno2 = 1;
                    }
                    if(Jobno2==1){
                        Integer prseq = projectMapper.getProjectSeq(paramVo);
                        prseq = prseq+1;
                        if(prseq > 9999){
                            return responseService.getFailResult("project_create","더이상 프로젝트를 생성할 수 없습니다.");
                        }
                        String a1 = ("000"+prseq);
                        String PRID = "P" + a1.substring(a1.length()-4,a1.length());
                        paramVo.setProject_seq(prseq);
                        paramVo.setProject_id(PRID);
                        Integer pridrs = projectMapper.savProjectJobProjectid(paramVo);
                        if(pridrs>0){
                            Integer rs = projectMapper.savProjectInfo2(paramVo);
                            if(rs == 0){
                                return responseService.getFailResult("project_create","저장을 할 수 없습니다.");
                            }else{
                                if(paramVo.getIdx_project()==0 || paramVo.getIdx_project() == null){
                                    return responseService.getFailResult("project_create","저장에 실패하였습니다.");
                                }else{
                                    // 요청 사항 : 2023.01.06 회의때 프로젝트 저장 시 리포트 생성까지 같이 되도록 요청
                                    Integer _seq = projectMapper.getReportSeq();
                                    _seq = _seq+1;
                                    String b1 = ("000"+_seq);
                                    String RPID = "R" + b1.substring(b1.length()-4,b1.length());
                                    ProjectVO param = new ProjectVO();
                                    param.setReport_seq(_seq);
                                    param.setReport_id(RPID);
                                    param.setIdx_user(paramVo.getIdx_user());
                                    param.setIdx_project(paramVo.getIdx_project());
                                    param.setIdx_project_job_projectid(paramVo.getIdx_project_job_projectid());
                                    param.setFilename(originFileName);
                                    param.setFilepath(path);
                                    Integer _rs0 = projectMapper.savReport(param);
                                    if(_rs0==1){
                                        projectService.create_report(req, param);
                                    }
                                    _rdata.put("idx_project",paramVo.getIdx_project());
                                    _rdata.put("idx_project_job_projectid",paramVo.getIdx_project_job_projectid());
                                }
                            }
                        }else{
                            return responseService.getFailResult("project_create","저장에 실패하였습니다.");
                        }
                    }else{
                        return responseService.getFailResult("project_create","저장에 실패하였습니다.");
                    }
                }
                _rdata.put("xlsdata",_elist);
                return responseService.getSuccessResult(_rdata, "project_create", "프로젝트 설정이 저장되었습니다.");
            }else{
                return responseService.getFailResult("project_create",".csv 포맷 파일이 맞는지 확인 후 다시 업로드를 시도해주세요.");
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
            Integer tcnt = projectMapper.getProjectListCount(paramVo);
            List<ProjectVO> rs = projectMapper.getProjectList(paramVo);
            Map<String, Object> _data = new HashMap<String, Object>();
            _data.put("tcnt",tcnt);
            _data.put("list",rs);
            if(rs!=null){
                return responseService.getSuccessResult(_data, "list_project", "프로젝트 리스팅 성공");
            }else{
                return responseService.getFailResult("list_project","프로젝트 리스트가 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("list_project","오류가 발생하였습니다.");
        }
    }

    /**
     * 프로젝트 상세보기
     * @param req
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/project_view")
    public CommonResult getProjectView(HttpServletRequest req, @RequestBody ProjectVO paramVo) throws Exception {
        try {
            if(StringUtils.isEmpty(paramVo.getIdx_project_job_projectid()+"")){
                return responseService.getFailResult("report_view","프로젝트 INDEX가 없습니다.");
            }

            List<ProjectVO> rs = projectMapper.getReportFileList(paramVo);
            ArrayList<ProjectViewVO> rlist = new ArrayList<ProjectViewVO>();
            for(ProjectVO prs0 : rs){
                rlist = projectService.get_projectListView(rlist, prs0);
            }
            if(rlist!=null){
                return responseService.getSuccessResult(rlist, "report_view", "프로젝트 정보 성공");
            }else{
                return responseService.getFailResult("report_view","프로젝트 정보가 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("report_view","오류가 발생하였습니다.");
        }
    }

    /**
     * 리포트 생성
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

    /**
     * 리포트 상세보기
     * @param req
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/report_view")
    public CommonResult getReportView(HttpServletRequest req, @RequestBody ProjectVO paramVo) throws Exception {
        try {
            if(StringUtils.isEmpty(paramVo.getIdx_project_job_projectid()+"")){
                return responseService.getFailResult("report_view","리포트 INDEX가 없습니다.");
            }

            ProjectVO rs0 = projectMapper.getProjectView(paramVo);
            ProjectVO rs1 = projectMapper.getReportView(paramVo);
            Map<String, Object> _data = new HashMap<String, Object>();
            _data.put("project",rs0);
            _data.put("report",rs1);
            if(rs1!=null){
                return responseService.getSuccessResult(_data, "report_view", "리포트 정보 성공");
            }else{
                return responseService.getFailResult("report_view","리포트 정보가 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("report_view","오류가 발생하였습니다.");
        }
    }
}
