package com.kantar.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kantar.mapper.FilterMapper;
import com.kantar.service.FileService;
import com.kantar.service.FilterService;
import com.kantar.service.StatisticsService;
import com.kantar.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
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
import jakarta.servlet.http.HttpServletResponse;

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
    private FileService fileService;

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
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo==null){
                return responseService.getFailResult("login","로그인이 필요합니다.");
            }
            paramVo.setIdx_user(uinfo.getIdx_user());

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
            _data.put("filter",filter0);
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

    /**
     * 리포트 다운로드
     * @param req
     * @param paramVo
     * @return void
     * @throws Exception
     */
    @GetMapping("/download")
    public void getReportDown(HttpServletResponse response, HttpServletRequest req, @RequestBody ProjectVO paramVo) throws Exception {
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo==null){
                return;
            }
            paramVo.setIdx_user(uinfo.getIdx_user());

            if(StringUtils.isEmpty(paramVo.getIdx_report()+"")){
                return;
            }

            paramVo.setIdx(paramVo.getIdx_report());
            ProjectVO rs0 = projectMapper.getProjectView(paramVo);
            List<FilterVO> filter0 = filterMapper.getReportFilterByIdx(paramVo.getIdx());
            List<ProjectVO> reportarr = reportMapper.getReportDataViewAll(paramVo);
            List<ReportFilterKeywordVO> key0 = reportMapper.getReportKeywordView(paramVo);

            Workbook wb = new HSSFWorkbook();
            Sheet sheet = wb.createSheet("REPORT_" + rs0.getProject_name());

            // 타이틀 스타일
            CellStyle style = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            font.setFontName("맑은 고딕");
            style.setWrapText(true); //문자열을 입력할때 \n 같은 개행을 인식해준다.
            // style.setVerticalAlignment(VerticalAlignment.CENTER);
            // style.setAlignment(HorizontalAlignment.CENTER);
            style.setFont(font);

            // 내용 스타일
            CellStyle style2 = wb.createCellStyle();
            Font font2 = wb.createFont();
            font2.setBold(false);
            font2.setFontName("맑은 고딕");
            style2.setWrapText(true); //문자열을 입력할때 \n 같은 개행을 인식해준다.
            style2.setFont(font2);

            int rowNum = 0;

            Row titleRow = sheet.createRow(rowNum); // 타이틀행을 생성한다. 첫번째줄이기때문에 createRow(0)
            Cell titleCell = titleRow.createCell(0); // 첫번째행의 첫번째열을 지정한다. 
            titleCell.setCellValue("01. 기본정보"); // setCellValue 셀에 값넣기.
            // titleRow.setHeight((short)920); // Row에서 setHeight를 하면 행 높이가 조정된다. 
            // sheet.addMergedRegion(new CellRangeAddress(0,0,0,9)); // 셀 병합  첫번째줄~아홉번째 열까지 병합 
            // new CellRangeAddress(시작 row, 끝 row, 시작 col, 끝 col) 
            titleCell.setCellStyle(style);
            
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue("리포트 이름");
            cell.setCellStyle(style);

            cell = row.createCell(1);
            cell.setCellValue("프로젝트 이름");
            cell.setCellStyle(style);

            cell = row.createCell(1);
            cell.setCellValue("생성일자");
            cell.setCellStyle(style);

            cell = row.createCell(1);
            cell.setCellValue("프로젝트 세부내용");
            cell.setCellStyle(style);

            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue("");
            cell.setCellStyle(style2);

            cell = row.createCell(1);
            cell.setCellValue(rs0.getProject_name());
            cell.setCellStyle(style2);

            cell = row.createCell(1);
            cell.setCellValue(rs0.getCreate_dt());
            cell.setCellStyle(style2);

            cell = row.createCell(1);
            cell.setCellValue(rs0.getSummary0());
            cell.setCellStyle(style2);

            rowNum++;

            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue("02. 적용된 필터값");
            cell.setCellStyle(style);

            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue("화자");
            cell.setCellStyle(style);

            cell = row.createCell(1);
            cell.setCellValue("챕터");
            cell.setCellStyle(style);

            cell = row.createCell(2);
            cell.setCellValue("서브챕터");
            cell.setCellStyle(style);

            cell = row.createCell(3);
            cell.setCellValue("질문");
            cell.setCellStyle(style);

            cell = row.createCell(4);
            cell.setCellValue("키워드");
            cell.setCellStyle(style);

            Integer rowNum0 = rowNum;
            Integer rowNum1 = rowNum;
            Integer rowNum2 = rowNum;
            Integer rowNum3 = rowNum;
            Integer rowNum4 = rowNum;

            // 화자
            for(int i = 0; i<filter0.size(); i++) {
                if(filter0.get(i).getFilter_type() == 1)
                for(int j = 0; j<filter0.get(i).getFilterDataList().size(); j++) {
                    row = sheet.createRow(rowNum0++);
                    cell = row.createCell(0);
                    cell.setCellValue(filter0.get(i).getFilterDataList().get(j).getFilter_data());
                    cell.setCellStyle(style2);
                }
            }

            // 챕터
            for(int i = 0; i<filter0.size(); i++) {
                if(filter0.get(i).getFilter_type() == 2)
                for(int j = 0; j<filter0.get(i).getFilterDataList().size(); j++) {
                    row = sheet.createRow(rowNum1++);
                    cell = row.createCell(1);
                    cell.setCellValue(filter0.get(i).getFilterDataList().get(j).getFilter_data());
                    cell.setCellStyle(style2);
                }
            }

            // 서브챕터
            for(int i = 0; i<filter0.size(); i++) {
                if(filter0.get(i).getFilter_type() == 2)
                for(int j = 0; j<filter0.get(i).getFilterDataList().size(); j++) {
                    row = sheet.createRow(rowNum2++);
                    cell = row.createCell(2);
                    cell.setCellValue(filter0.get(i).getFilterDataList().get(j).getFilter_data());
                    cell.setCellStyle(style2);
                }
            }

            // 질문
            for(int i = 0; i<filter0.size(); i++) {
                if(filter0.get(i).getFilter_type() == 2)
                for(int j = 0; j<filter0.get(i).getFilterDataList().size(); j++) {
                    row = sheet.createRow(rowNum3++);
                    cell = row.createCell(3);
                    cell.setCellValue(filter0.get(i).getFilterDataList().get(j).getFilter_data());
                    cell.setCellStyle(style2);
                }
            }

            // 키워드
            for(int i = 0; i<filter0.size(); i++) {
                if(filter0.get(i).getFilter_type() == 2)
                for(int j = 0; j<filter0.get(i).getFilterDataList().size(); j++) {
                    row = sheet.createRow(rowNum4++);
                    cell = row.createCell(4);
                    cell.setCellValue(filter0.get(i).getFilterDataList().get(j).getFilter_data());
                    cell.setCellStyle(style2);
                }
            }

            rowNum = rowNum0;
            if(rowNum1 > rowNum){rowNum=rowNum1;}
            if(rowNum2 > rowNum){rowNum=rowNum2;}
            if(rowNum3 > rowNum){rowNum=rowNum3;}
            if(rowNum4 > rowNum){rowNum=rowNum4;}

            rowNum++;

            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue("03. 요약문");
            cell.setCellStyle(style);
            
            Integer _rpNum0 = rowNum++;
            Integer _rpNum1 = 0;

            for(ProjectVO _rs : reportarr){
                row = sheet.createRow(_rpNum0);
                cell = row.createCell(_rpNum1);
                cell.setCellValue(_rs.getTitle());
                cell.setCellStyle(style);
                
                Row row1 = sheet.createRow(_rpNum0+1);
                cell = row1.createCell(_rpNum1);
                cell.setCellValue(_rs.getSummary0());
                cell.setCellStyle(style2);

                _rpNum1++;
            }

            rowNum++;

            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue("04. 키워드 빈도");
            cell.setCellStyle(style);

            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue("키워드 이름");
            cell.setCellStyle(style);

            cell = row.createCell(1);
            cell.setCellValue("형태");
            cell.setCellStyle(style);

            cell = row.createCell(2);
            cell.setCellValue("건수");
            cell.setCellStyle(style);

            for(ReportFilterKeywordVO _rs : key0){
                row = sheet.createRow(rowNum++);
                cell = row.createCell(0);
                cell.setCellValue(_rs.getSum_keyword());
                cell.setCellStyle(style2);

                cell = row.createCell(1);
                cell.setCellValue(_rs.getKeytype());
                cell.setCellStyle(style2);

                cell = row.createCell(2);
                cell.setCellValue(_rs.getKeycount());
                cell.setCellStyle(style2);
            }

            rowNum++;

            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue("05. 챕터별 메타 데이터");
            cell.setCellStyle(style);

            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue("챕터");
            cell.setCellStyle(style);

            cell = row.createCell(1);
            cell.setCellValue("화자");
            cell.setCellStyle(style);

            cell = row.createCell(2);
            cell.setCellValue("건수");
            cell.setCellStyle(style);

            cell = row.createCell(3);
            cell.setCellValue("텍스트 길이");
            cell.setCellStyle(style);


            rowNum++;

            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue("06. 화자별 메타 데이터");
            cell.setCellStyle(style);

            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue("화자");
            cell.setCellStyle(style);

            cell = row.createCell(1);
            cell.setCellValue("건수");
            cell.setCellStyle(style);

            cell = row.createCell(2);
            cell.setCellValue("텍스트 길이");
            cell.setCellStyle(style);


            rowNum++;

            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue("07. 사용자 메모");
            cell.setCellStyle(style);

            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue("메모");
            cell.setCellStyle(style);

            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(rs0.getSummary());
            cell.setCellStyle(style2);
                
            /* 엑셀 파일 생성 */
            response.setContentType("ms-vnd/excel");
            response.setHeader("Content-Disposition", "attachment;filename=poiTest.xls");
            wb.write(response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
