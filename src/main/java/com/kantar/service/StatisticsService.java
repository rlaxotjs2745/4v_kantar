package com.kantar.service;

import com.google.gson.Gson;
import com.kantar.mapper.ProjectMapper;
import com.kantar.mapper.ReportMapper;
import com.kantar.mapper.StatisticsMapper;
import com.kantar.util.Excel;
import com.kantar.util.Summary;
import com.kantar.util.TokenJWT;
import com.kantar.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class StatisticsService {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private final StatisticsMapper statisticsMapper;

    @Autowired
    private Excel excel;

    @Value("${file.upload-dir}")
    public String filepath;

    public StatisticsVO getCurrentFileStatistic() throws Exception {

        StatisticsVO statisticsVO = new StatisticsVO();

        long fileCnt = statisticsMapper.getFileCount();
        long wordCnt = statisticsMapper.getAllFileWordCnt();
        Double sizeSum = statisticsMapper.getAllFileSize();
        long reportCnt = statisticsMapper.getReportCount();

        statisticsVO.setFile_cnt(fileCnt);
        statisticsVO.setWord_length(wordCnt);
        statisticsVO.setFile_size(sizeSum);
        statisticsVO.setReport_cnt(reportCnt);

        return statisticsVO;
    }

    /**
     * 시스템 사용 현황 - 기본리포트 생성 될 때(=프로젝트 생성 될 때)
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    public StatisticsVO createProjectInfo(ProjectVO paramVo) throws Exception {

        StatisticsVO statisticsVO = new StatisticsVO();

        if(paramVo.getIdx_project()!=null && paramVo.getIdx_project()>0){
            List<ProjectVO> prs = reportMapper.getReportFileList(paramVo);

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
     * 시스템 사용 현황 - 추가리포트 생성 할 때 (리포트 수 증감. 누적 기준이기에 삭제는 반영안함)
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    public void updateReportCnt(ProjectVO paramVo) throws Exception {
        if(paramVo.getIdx_project()!=null && paramVo.getIdx_project()>0){
            statisticsMapper.updateProjectReporteCnt(paramVo);
        }
    }

    /**
     * 시스템 사용 현황 - 프로젝트 삭제
     * ('누적'기준이기에 사용안함. 해당 api로 삭제기록 하더라도 전체카운트에는 잡히도록 함)
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    public void deleteProjectInfo(ProjectVO paramVo) throws Exception {
        if(paramVo.getIdx_project()!=null && paramVo.getIdx_project()>0){
            statisticsMapper.deleteProjectStatistics(paramVo);
        }
    }

    /**
     * API 사용량 (키워드 수 직접 입력)
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    public void getAPIUsage(Integer idx_report, Integer type, Integer word_length) throws Exception {
        if(idx_report!=null && idx_report>0){

            StatisticsVO statisticsVO = new StatisticsVO();
            statisticsVO.setIdx_report(idx_report);
            statisticsVO.setApi_type(type);
            int ori_date = statisticsMapper.getReportAPIUsage(idx_report);

            if(ori_date==0){
                if(type==1){
                    statisticsVO.setSummaryUsage(word_length);
                    statisticsMapper.setSummaryAPIUsage(statisticsVO);
                }
                if(type==2){
                    statisticsVO.setKeywordUsage(word_length);
                    statisticsMapper.setKeywordAPIUsage(statisticsVO);
                }
            } else{
                if(type==1){
                    statisticsVO.setSummaryUsage(word_length);
                    statisticsMapper.updateSummaryAPIUsage(statisticsVO);
                }
                if(type==2){
                    statisticsVO.setKeywordUsage(word_length);
                    statisticsMapper.updateKeywordAPIUsage(statisticsVO);
                }
            }
        }
    }

    /**
     * API 사용량 (키워드 입력)
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    public void getAPIUsage(Integer idx_report, Integer type, String word) throws Exception {
        if(idx_report!=null && idx_report>0){

            StatisticsVO statisticsVO = new StatisticsVO();
            statisticsVO.setIdx_report(idx_report);
            statisticsVO.setApi_type(type);
            int ori_date = statisticsMapper.getReportAPIUsage(idx_report);
            int word_length = word.length();

            if(ori_date==0){
                if(type==1){
                    statisticsVO.setSummaryUsage(word_length);
                    statisticsMapper.setSummaryAPIUsage(statisticsVO);
                }
                if(type==2){
                    statisticsVO.setKeywordUsage(word_length);
                    statisticsMapper.setKeywordAPIUsage(statisticsVO);
                }
            } else{
                if(type==1){
                    statisticsVO.setSummaryUsage(word_length);
                    statisticsMapper.updateSummaryAPIUsage(statisticsVO);
                }
                if(type==2){
                    statisticsVO.setKeywordUsage(word_length);
                    statisticsMapper.updateKeywordAPIUsage(statisticsVO);
                }
            }
        }
    }
}
