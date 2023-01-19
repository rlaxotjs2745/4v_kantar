package com.kantar.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.kantar.vo.ProjectListVO;
import com.kantar.vo.ProjectVO;
import com.kantar.vo.ProjectViewVO;
import com.kantar.vo.UserVO;

import jakarta.servlet.http.HttpServletRequest;
// import lombok.RequiredArgsConstructor;

// @RequiredArgsConstructor
@RestController
@RequestMapping("/api/project")
public class ProjectController extends BaseController {
    @Autowired
    private ResponseService responseService;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private ProjectService projectService;

    @Value("${file.upload-dir}")
    public String filepath;

    /**
     * Chapter validation 클릭 시 csv 파일 업로드 : csv 저장 후 Job No, 프로젝트 이름 리턴 - 파일은 삭제
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/csv_view")
    public CommonResult viewCsv(MultipartHttpServletRequest req, ProjectVO paramVo) throws Exception {
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo==null){
                return responseService.getFailResult("login","로그인이 필요합니다.");
            }
            paramVo.setIdx_user(uinfo.getIdx_user());
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
                            return responseService.getFailResult("csv_view",".csv 포맷 파일이 맞는지 확인 후 다시 업로드를 시도해주세요.");
                        }
                        if(!contentType.equals("text/csv")) {
                            return responseService.getFailResult("csv_view",".csv 포맷 파일이 맞는지 확인 후 다시 업로드를 시도해주세요.");
                        }
                    }
                }

                String path = "/report/temp/";
                String fullpath = this.filepath + path;
                File fileDir = new File(fullpath);
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }

                List<ProjectViewVO> _elist = new ArrayList<ProjectViewVO>();
                for(MultipartFile mf : fileList) {
                    String originFileName = mf.getOriginalFilename();   // 원본 파일 명
                    mf.transferTo(new File(fullpath, originFileName));
                    _elist = projectService.getCsvParse(_elist, fullpath + originFileName);
                    File file = new File(fullpath + originFileName);
                    if (!file.exists()) {
                        file.delete();
                    }
                }
                Map<String, Map<String, Map<String, Map<String, Set<String>>>>> _ers = new HashMap<String, Map<String, Map<String, Map<String, Set<String>>>>>();
                if(_elist.size() > 0){
                    _ers = _elist.stream()
                    .sorted(
                        Comparator.comparing(ProjectViewVO::getChapter)
                            .thenComparing(ProjectViewVO::getSubchapter)
                            .thenComparing(ProjectViewVO::getQuestion)
                            .thenComparing(ProjectViewVO::getPerson)
                        )
                    .collect(
                        Collectors.groupingBy(ProjectViewVO::getChapter
                            , Collectors.groupingBy(ProjectViewVO::getSubchapter
                                , Collectors.groupingBy(ProjectViewVO::getQuestion
                                    , Collectors.groupingBy(ProjectViewVO::getPerson
                                        , Collectors.mapping(ProjectViewVO::getAnswer, Collectors.toSet())
                                    )
                                )
                            )
                        )
                    );
                }
                return responseService.getSuccessResult(_ers, "csv_view", "csv 파일 정보를 가져왔습니다.");
            }else{
                return responseService.getFailResult("csv_view",".csv 포맷 파일이 맞는지 확인 후 다시 업로드를 시도해주세요.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("csv_view","오류가 발생하였습니다.");
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
    public CommonResult getProjectList(HttpServletRequest req, @RequestBody ProjectVO paramVo) throws Exception {
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
            Integer tcnt = projectMapper.getProjectListCount(paramVo);
            List<ProjectListVO> rs = projectMapper.getProjectList(paramVo);
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
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo==null){
                return responseService.getFailResult("login","로그인이 필요합니다.");
            }
            paramVo.setIdx_user(uinfo.getIdx_user());
            if(StringUtils.isEmpty(paramVo.getIdx_project()+"")){
                return responseService.getFailResult("project_view","프로젝트 INDEX가 없습니다.");
            }

            List<ProjectVO> rs = reportMapper.getReportFileList(paramVo);
            List<ProjectViewVO> rlist = new ArrayList<ProjectViewVO>();
            for(ProjectVO prs0 : rs){
                rlist = projectService.get_projectListView(rlist, prs0);
            }
            if(rlist!=null){
                return responseService.getSuccessResult(rlist, "project_view", "프로젝트 정보 성공");
            }else{
                return responseService.getFailResult("project_view","프로젝트 정보가 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("project_view","오류가 발생하였습니다.");
        }
    }
}
