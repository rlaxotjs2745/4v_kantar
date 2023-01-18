package com.kantar.controller;

import com.kantar.base.BaseController;
import com.kantar.mapper.DictionaryMapper;
import com.kantar.mapper.ProjectMapper;
import com.kantar.mapper.StatisticsMapper;
import com.kantar.mapper.UserMapper;
import com.kantar.model.CommonResult;
import com.kantar.service.DictionaryService;
import com.kantar.service.ResponseService;
import com.kantar.util.Excel;
import com.kantar.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController extends BaseController {
    @Autowired
    private final ResponseService responseService;

    @Autowired
    private final UserMapper userMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private final StatisticsMapper statisticsMapper;

    @Value("${file.upload-dir}")
    public String filepath;

    @Autowired
    private Excel excel;

    /**
     * 시스템 사용 현황
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/system_statistics")
    public CommonResult getSystemList(HttpServletRequest req, @RequestBody UserVO paramVo) throws Exception {
        try{
            UserVO userInfo = userMapper.getUserInfo(paramVo);
            if(userInfo.getUser_type() == 1){
                return responseService.getFailResult("system_statistics","관리자만 조회 가능한 기능힙니다.");
            }

            long fileCnt = statisticsMapper.getFileCount();
            long wordCnt = statisticsMapper.getAllFileWordCnt();
            Double sizeSum = statisticsMapper.getAllFileSize();
            long reportCnt = statisticsMapper.getReportCount();

            StatisticsVO statisticsVO = new StatisticsVO();
            statisticsVO.setFile_cnt(fileCnt);
            statisticsVO.setWord_length(wordCnt);
            statisticsVO.setFile_size(sizeSum);
            statisticsVO.setReport_cnt(reportCnt);

            return responseService.getSuccessResult(statisticsVO, "system_statistics", "시스템 사용 현황이 정상적으로 존재합니다");

        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("list_dictionary","오류가 발생하였습니다.");
        }
    }

    /**
     * 시스템 사용 현황 - 기본리포트 생성 될 때(=프로젝트 생성 될 때)
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    public StatisticsVO createProjectInfo(ProjectVO paramVo) throws Exception {

        StatisticsVO statisticsVO = new StatisticsVO();

        if(paramVo.getIdx_project()>0){
            List<ProjectVO> prs = projectMapper.getReportFileList(paramVo);

            double sizeSum = 0;
            long lengthSum = 0;

            if(!prs.isEmpty()){
                for(ProjectVO prs0 : prs) {
                    String _fpath = this.filepath + prs0.getFilepath() + prs0.getFilename();

                    File dir = new File(_fpath);
                    sizeSum += dir.length();

                    List<String[]> ers = excel.getCsvListData(_fpath);

                    int j = 0;
                    for (String[] _ers0 : ers) {  // 열
                        if (j > 0) {
                            for (String _ers00 : _ers0) {
                                lengthSum += _ers00.length();
                            }
                        }
                        j++;
                    }
                }
            }
            statisticsVO.setIdx_project(paramVo.getIdx_project());
            statisticsVO.setFile_cnt(prs.size());
            statisticsVO.setWord_length(lengthSum);
            statisticsVO.setFile_size(sizeSum/(1024*1024));

            statisticsMapper.setProjectStatistics(statisticsVO);
        }
        return statisticsVO;
    }

    /**
     * 시스템 사용 현황 - 추가리포트 생성/삭제 할 때 (리포트 수 증감)
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    public int updateReportCnt(ProjectVO paramVo) throws Exception {
        int num = 0;
        if(paramVo.getIdx_project()>0){
            num = statisticsMapper.updateProjectReporteCnt(paramVo);
        }
        return num;
    }

    /**
     * 시스템 사용 현황 - 프로젝트 삭제
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    public int deleteProjectInfo(ProjectVO paramVo) throws Exception {
        int num = 0;
        if(paramVo.getIdx_project()>0){
            num = statisticsMapper.deleteProjectStatistics(paramVo);
        }
        return num;
    }
}
