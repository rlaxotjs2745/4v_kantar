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

    /**
     * 현재 데이터 사용량 가져오기
     * @return StatisticsVO
     * @throws Exception
     */
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
     * @return StatisticsVO
     * @throws Exception
     */
    public StatisticsVO createProjectInfo(ProjectVO paramVo) throws Exception {

        StatisticsVO statisticsVO = new StatisticsVO();

        if(paramVo.getIdx_project()!=null && paramVo.getIdx_project()>0){

            double sizeSum = 0;
            long lengthSum = 0;

            String _fpath = this.filepath + paramVo.getFilepath() + paramVo.getFilename();

            File dir = new File(_fpath);
            sizeSum += dir.length();
            List<String[]> ers = excel.getCsvListData(_fpath);

            int j = 0;
            for (String[] _ers0 : ers) {  // 열
                if (j > 0) {
                    for (String _ers00 : _ers0) {
                        if(_ers00!=null){
                            lengthSum += _ers00.length();
                        }
                    }
                }
                j++;
            }

            statisticsVO.setIdx_user(paramVo.getIdx_user());
            statisticsVO.setIdx_project(paramVo.getIdx_project());
            statisticsVO.setFile_cnt(1);
            statisticsVO.setWord_length(lengthSum);
            statisticsVO.setFile_size(sizeSum/(1024*1024));

            statisticsMapper.setProjectStatistics(statisticsVO);
        }
        return statisticsVO;
    }

    /**
     * 사용 안함 (API 생성로직에 추가)
     * 시스템 사용 현황 - 추가리포트 생성 할 때 (리포트 수 증감. 누적 기준이기에 삭제는 반영안함)
     * @param paramVo
     * @throws Exception
     */
    public void updateReportCnt(ProjectVO paramVo) throws Exception {
        if(paramVo.getIdx_project()!=null && paramVo.getIdx_project()>0){
            statisticsMapper.updateProjectReporteCnt(paramVo);
        }
    }

    /**
     * 사용 안함 (누적 기준이라 삭제 개념이 없음)
     * 시스템 사용 현황 - 프로젝트 삭제
     * @param paramVo
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
     * @param type
     * @param word_length
     * @throws Exception
     */
    public void createAPIUsage(ProjectVO paramVo, Integer type, Integer word_length) throws Exception {
        if(paramVo.getIdx_report()!=null && paramVo.getIdx_report()>0){

            StatisticsVO statisticsVO = new StatisticsVO();
            statisticsVO.setIdx_report(paramVo.getIdx_report());
            statisticsVO.setIdx_user(paramVo.getIdx_user());
            statisticsVO.setApi_type(type);
            int ori_date = statisticsMapper.getReportAPIUsage(statisticsVO);

            if(ori_date==0){
                ProjectVO param = statisticsMapper.getPjIdxToReport(statisticsVO);
                statisticsMapper.updateProjectReporteCnt(param);
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
                    //statisticsMapper.updateSummaryAPIUsage(statisticsVO);
                    statisticsMapper.setSummaryAPIUsageAdd(statisticsVO);
                }
                if(type==2){
                    statisticsVO.setKeywordUsage(word_length);
                    //statisticsMapper.updateKeywordAPIUsage(statisticsVO);
                    statisticsMapper.setKeywordAPIUsageAdd(statisticsVO);
                }
            }
        }
    }

    /**
     * API 사용량 (키워드 입력)
     * @param paramVo
     * @param type
     * @param word
     * @throws Exception
     */
    public void createAPIUsage(ProjectVO paramVo, Integer type, String word) throws Exception {
        if(paramVo.getIdx_report()!=null && paramVo.getIdx_report()>0){

            StatisticsVO statisticsVO = new StatisticsVO();
            statisticsVO.setIdx_report(paramVo.getIdx_report());
            statisticsVO.setIdx_user(paramVo.getIdx_user());
            statisticsVO.setApi_type(type);
            int ori_date = statisticsMapper.getReportAPIUsage(statisticsVO);
            int word_length = word.length();

            if(ori_date==0){
                ProjectVO param = statisticsMapper.getPjIdxToReport(statisticsVO);
                statisticsMapper.updateProjectReporteCnt(param);
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
                    statisticsMapper.setSummaryAPIUsageAdd(statisticsVO);
                }
                if(type==2){
                    statisticsVO.setKeywordUsage(word_length);
                    statisticsMapper.setKeywordAPIUsageAdd(statisticsVO);
                }
            }
        }
    }
}
