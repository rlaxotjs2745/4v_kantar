package com.kantar.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kantar.mapper.FilterMapper;
import com.kantar.service.FilterService;
import com.kantar.service.StatisticsService;
import com.kantar.vo.*;
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
import com.kantar.mapper.ReportMapper;
import com.kantar.model.CommonResult;
import com.kantar.service.ProjectService;
import com.kantar.service.ResponseService;
import com.kantar.util.TokenJWT;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/report")
public class ReportController extends BaseController {
    @Autowired
    private ResponseService responseService;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private FilterService filterService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private FilterMapper filterMapper;

    @Autowired
    private TokenJWT tokenJWT;

    @Value("${file.upload-dir}")
    public String filepath;

    /**
     * 리포트 생성 : 필터 적용하기
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/create")
    public CommonResult createReportWithFilter(HttpServletRequest req, ReportVO paramVo) throws Exception {
        String _token = tokenJWT.resolveToken(req);
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo==null){
                return responseService.getFailResult("login","로그인이 필요합니다.");
            }
            paramVo.setIdx_user(uinfo.getIdx_user());
            if(StringUtils.isEmpty(paramVo.getTitle())){
                return responseService.getFailResult("report_create","리포트 이름을 입력해주세요.");
            }
            if(StringUtils.isEmpty(paramVo.getIdx_project()+"")){
                return responseService.getFailResult("report_create","프로젝트 값이 없습니다.");
            }
            ProjectVO param = new ProjectVO();
            param.setIdx_user(paramVo.getIdx_user());
            param.setIdx_project(paramVo.getIdx_project());
            param.setIdx_project_job_projectid(paramVo.getIdx_project_job_projectid());
            param.setTitle(paramVo.getTitle());
            projectService.create_report(_token, param, 1);
            return responseService.getSuccessResult("create", "리포트 생성을 시작하였습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("create","오류가 발생하였습니다.");
        }
    }

    /**
     * 리포트 생성 : CSV 파일 업로드
     * @param req
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/create_report")
    @Transactional
    public CommonResult create_report(MultipartHttpServletRequest req, ProjectVO paramVo) throws Exception {
        String _token = tokenJWT.resolveToken(req);
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo==null){
                return responseService.getFailResult("login","로그인이 필요합니다.");
            }
            paramVo.setIdx_user(uinfo.getIdx_user());
            if(StringUtils.isEmpty(paramVo.getJob_no())){
                return responseService.getFailResult("create_report","JOB No를 입력해주세요.");
            }
            if(StringUtils.isEmpty(paramVo.getProject_name())){
                return responseService.getFailResult("create_report","프로젝트 이름을 입력해주세요.");
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
                            return responseService.getFailResult("create_report",".csv 포맷 파일이 맞는지 확인 후 다시 업로드를 시도해주세요.");
                        }
                        if(contentType != null && !contentType.equals("text/csv")) {
                            return responseService.getFailResult("create_report",".csv 포맷 파일이 맞는지 확인 후 다시 업로드를 시도해주세요.");
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
                            return responseService.getFailResult("create_report","더이상 프로젝트를 생성할 수 없습니다.");
                        }
                        String a1 = ("000"+prseq);
                        String PRID = "P" + a1.substring(a1.length()-4,a1.length());
                        paramVo.setProject_seq(prseq);
                        paramVo.setProject_id(PRID);
                        Integer pridrs = projectMapper.savProjectJobProjectid(paramVo);
                        if(pridrs>0){
                            Integer rs = projectMapper.savProjectInfo(paramVo);
                            if(rs == 0){
                                return responseService.getFailResult("create_report","저장을 할 수 없습니다.");
                            }else{
                                if(paramVo.getIdx_project()==0 || paramVo.getIdx_project() == null){
                                    return responseService.getFailResult("create_report","저장에 실패하였습니다.");
                                }else{
                                    paramVo.setFilepath(path);
                                    paramVo.setFilename(originFileName);
                                    statisticsService.createProjectInfo(paramVo);// 프로젝트 생성시 사용량 누적

                                    // 요청 사항 : 2023.01.06 회의때 프로젝트 저장 시 리포트 생성까지 같이 되도록 요청
                                    Integer _seq = reportMapper.getReportSeq();
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
                                    param.setProject_name(paramVo.getProject_name());
                                    param.setTitle(paramVo.getProject_name() + "_기본리포트");
                                    Integer _rs0 = reportMapper.savReport(param);
                                    if(_rs0==1){
                                        projectService.create_report(_token, param, 0);
                                    }
                                    _rdata.put("idx_project",paramVo.getIdx_project());
                                    _rdata.put("idx_project_job_projectid",paramVo.getIdx_project_job_projectid());
                                }
                            }
                        }else{
                            return responseService.getFailResult("create_report","리포트 기본 설정 저장에 실패하였습니다.");
                        }
                    }else{
                        return responseService.getFailResult("create_report","리포트 기본 설정 저장에 실패하였습니다.");
                    }
                }
                _rdata.put("xlsdata",_elist);
                return responseService.getSuccessResult(_rdata, "create_report", "기본 리포트 생성을 시작하였습니다.");
            }else{
                return responseService.getFailResult("create_report",".csv 포맷 파일이 맞는지 확인 후 다시 업로드를 시도해주세요.");
            }
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
    public CommonResult getReportList(HttpServletRequest req, @RequestBody ProjectVO paramVo) throws Exception {
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo==null){
                return responseService.getFailResult("login","로그인이 필요합니다.");
            }
            paramVo.setIdx_user(uinfo.getIdx_user());
            if(paramVo.getCurrentPage() == null){
                paramVo.setCurrentPage(1);
            }
            paramVo.setRecordCountPerPage(10);
            paramVo.setFirstIndex((paramVo.getCurrentPage()-1) * 10);
            Integer tcnt = reportMapper.getReportListCount(paramVo);
            List<ProjectVO> rs = reportMapper.getReportList(paramVo);
            Map<String, Object> _data = new HashMap<String, Object>();
            _data.put("tcnt",tcnt);
            _data.put("list",rs);
            if(rs!=null){
                return responseService.getSuccessResult(_data, "list_report", "리포트 리스팅 성공");
            }else{
                return responseService.getFailResult("list_report","리포트 리스트가 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("list_report","오류가 발생하였습니다.");
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
            /*
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo==null){
                return responseService.getFailResult("login","로그인이 필요합니다.");
            }
            paramVo.setIdx_user(uinfo.getIdx_user());

             */

            if(StringUtils.isEmpty(paramVo.getIdx()+"")){
                return responseService.getFailResult("report_view","리포트 INDEX가 없습니다.");
            }

            ProjectVO rs0 = projectMapper.getProjectView(paramVo); // 프로젝트 기본정보
            List<ProjectVO> rs1 = reportMapper.getReportDataViewAll(paramVo); // 리포트 기본정보
            List<ReportFilterKeywordVO> key0 = reportMapper.getReportKeywordView(paramVo); // 키워드
            if(key0.size()>0){
                for (ReportFilterKeywordVO _keyword : key0) {
                    paramVo.setKeywords(_keyword.getSum_keyword());
                    int _dicCnt = reportMapper.getKeywordFindDictionary(paramVo);
                    if (_dicCnt > 0){
                        _keyword.setDic_yn(1);
                    } else {  _keyword.setDic_yn(0);}
                }
            }
            List<FilterVO> filter0 = filterMapper.getReportFilterByIdx(paramVo.getIdx()); // 필터 조건
            List<ReportMetaDataVO> metaSpeaker = reportMapper.getMetadataInfoSpeaker(paramVo.getIdx());
            List<ReportMetaDataVO> metaChapter = reportMapper.getMetadataInfoChapter(paramVo.getIdx());

            Map<String, Object> _data = new HashMap<String, Object>();
            _data.put("project",rs0);
            _data.put("report",rs1);
            _data.put("keyword",key0);
            _data.put("metaSpeaker",metaSpeaker);
            _data.put("metaChapter",metaChapter);

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

    @PostMapping("/report_modify")
    public CommonResult modiReportView(HttpServletRequest req, @RequestBody ProjectVO paramVo) throws Exception {
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo==null){
                return responseService.getFailResult("login","로그인이 필요합니다.");
            }
            paramVo.setIdx_user(uinfo.getIdx_user());
            if(StringUtils.isEmpty(paramVo.getIdx_project_job_projectid()+"")){
                return responseService.getFailResult("report_view","리포트 INDEX가 없습니다.");
            }

            Integer rs0 = projectMapper.modiProjectInfo(paramVo);
            Integer rs1 = reportMapper.modiReportData(paramVo);
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




    /**
     * 병합 리포트 생성 : CSV 파일 병합
     * @param req
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/merge_report")
    @Transactional
    public CommonResult merge_report(HttpServletRequest req, @RequestBody ProjectVO paramVo) throws Exception {
        String _token = tokenJWT.resolveToken(req);
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo==null){
                return responseService.getFailResult("merge_report","로그인이 필요합니다.");
            }
            paramVo.setIdx_user(uinfo.getIdx_user());

            if(StringUtils.isEmpty(paramVo.getJob_no())){
                return responseService.getFailResult("merge_report","JOB No를 입력해주세요.");
            }
            if(StringUtils.isEmpty(paramVo.getProject_name())){
                ProjectVO m0 = new ProjectVO();
                String[] _mergeIdx = paramVo.getProject_merge_idx().split(",");

                m0.setIdx_project(Integer.valueOf(String.valueOf(_mergeIdx[0])));
                List<ProjectVO> prs = reportMapper.getReportFileListOne(m0);

                String fileName = prs.get(0).getFilename();
                String fileNameEx = fileName.substring(0, fileName.lastIndexOf('.'));
                paramVo.setProject_name(fileNameEx);
            }

            File mergeCsv = projectService.merge_csv(paramVo);

            ArrayList<Object> _elist = new ArrayList<Object>();
            Map<String, Object> _rdata = new HashMap<String, Object>();

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
                    return responseService.getFailResult("merge_report","더이상 프로젝트를 생성할 수 없습니다.");
                }
                String a1 = ("000"+prseq);
                String PRID = "P" + a1.substring(a1.length()-4,a1.length());
                paramVo.setProject_seq(prseq);
                paramVo.setProject_id(PRID);
                Integer pridrs = projectMapper.savProjectJobProjectid(paramVo);
                if(pridrs>0){
                    Integer rs = projectMapper.savMergeProject(paramVo);
                    if(rs == 0){
                        return responseService.getFailResult("merge_report","저장을 할 수 없습니다.");
                    }else{
                        if(paramVo.getIdx_project()==0 || paramVo.getIdx_project() == null){
                            return responseService.getFailResult("merge_report","저장에 실패하였습니다.");
                        }else{
                            Integer _seq = reportMapper.getReportSeq();
                            _seq = _seq+1;
                            String b1 = ("000"+_seq);
                            String RPID = "R" + b1.substring(b1.length()-4,b1.length());
                            ProjectVO result = new ProjectVO();
                            result.setReport_seq(_seq);
                            result.setReport_id(RPID);
                            result.setIdx_user(paramVo.getIdx_user());
                            result.setIdx_project(paramVo.getIdx_project());
                            result.setIdx_project_job_projectid(paramVo.getIdx_project_job_projectid());
                            result.setFilename(mergeCsv.getName());
                            result.setFilepath("/report/" + paramVo.getJob_no() + "/");
                            result.setProject_name(paramVo.getProject_name());
                            result.setTitle(paramVo.getProject_name() + "_병합리포트");
                            Integer _rs0 = reportMapper.savReport(result);
                            if(_rs0==1){
                                projectService.create_report(_token, result, 0);
                            }
                            _rdata.put("idx_project",paramVo.getIdx_project());
                            _rdata.put("idx_project_job_projectid",paramVo.getIdx_project_job_projectid());
                        }
                    }
                }else{
                    return responseService.getFailResult("merge_report","리포트 기본 설정 저장에 실패하였습니다.");
                }
            }else{
                return responseService.getFailResult("merge_report","리포트 기본 설정 저장에 실패하였습니다.");
                    }
        _rdata.put("xlsdata",_elist);
        return responseService.getSuccessResult(_rdata, "merge_report", "기본 리포트 생성을 시작하였습니다.");

        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("merge_report","오류가 발생하였습니다.");
        }
    }

    @PostMapping("/save_filter_report")
    @Transactional
    public CommonResult save_filter_report(HttpServletRequest req, @RequestBody ReportFilterDataVO filterVO) throws Exception {
        String _token = tokenJWT.resolveToken(req);
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo==null){
                return responseService.getFailResult("save_filter_report","로그인이 필요합니다.");
            }

            filterVO.setIdx_user(uinfo.getIdx_user());

            if(StringUtils.isEmpty(filterVO.getReport_name())){
                return responseService.getFailResult("save_filter_report","리포트 이름을 입력해주세요.");
            }

            if(filterVO.getIdx_project()==null || filterVO.getIdx_project_job_projectid()==null){
                return responseService.getFailResult("save_filter_report","프로젝트 정보를 다시 확인해주세요");
            }

            if(StringUtils.isEmpty(filterVO.getTp1()) && StringUtils.isEmpty(filterVO.getTp2()) && StringUtils.isEmpty(filterVO.getTp3()) && StringUtils.isEmpty(filterVO.getTp4())){
                return responseService.getFailResult("filter_create","필터 데이터가 없습니다.");
            }

            if(filterVO.getFilter_op2()==null || filterVO.getFilter_op2()>3 || filterVO.getFilter_op2()<1){
                return responseService.getFailResult("filter_create","지정된 키워드 필터 옵션이 올바르지 않습니다.");
            }

            Integer idx_filter = filterService.createReportFilter(filterVO);
            filterVO.setIdx_filter(idx_filter);
            projectService.list_reportfilter(_token, filterVO);


            return responseService.getSuccessResult("test_report", "필터 리포트 생성을 시작하였습니다.");

        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("test_report","오류가 발생하였습니다.");
        }
    }

}
