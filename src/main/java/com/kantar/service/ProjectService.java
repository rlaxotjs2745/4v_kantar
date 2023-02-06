package com.kantar.service;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.kantar.mapper.FilterMapper;
import com.kantar.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.kantar.mapper.ReportMapper;
import com.kantar.util.Excel;
import com.kantar.util.Summary;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ProjectService {
    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private FilterMapper filterMapper;

    @Autowired
    private Excel excel;

    @Autowired
    private Summary summary;

    // @Autowired
    // private KafkaSender kafkaSender;
    
    @Value("${file.upload-dir}")
    public String filepath;

    @Value("${spring.smr.token}")
    public String smrtoken;

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 비동기 리포트 결과 만들기
     * @param paramVo
     * @param _tp
     * @throws Exception
     */
    @Async
    @Transactional
    public void create_report(String _token, ProjectVO paramVo, Integer _tp) throws Exception{
        String _msg = "";
        try {
            List<ProjectVO> prs = reportMapper.getReportFileList(paramVo);

            for(ProjectVO prs0 : prs){
                String _fpath = this.filepath + prs0.getFilepath() + prs0.getFilename();
                
                List<SumtextVO> _data = new ArrayList<SumtextVO>();

                Map<String, Object> _nlp = new HashMap<String, Object>();
                Map<String, Object> _nlp0 = new HashMap<String, Object>();
                Map<String, Object> _nlp1 = new HashMap<String, Object>();
                Map<String, Object> _nlp2 = new HashMap<String, Object>();
                SummaryVO params = new SummaryVO();
                List<String[]> ers = excel.getCsvListData(_fpath);
                int j = 0;
                for(String[] _ers0 : ers){  // 줄
                    if(j>0){
                        int i = 0;
                        SumtextVO _elist = new SumtextVO();
                        for(String _ers00 : _ers0){ // 컬럼
                            if(i==3){
                                _elist.setSpeaker(_ers00.toString());
                            }
                            if(i==4){
                                _elist.setText(_ers00.toString());
                            }
                            i++;
                        }
                        _data.add(_elist);
                    }
                    j++;
                }

                params.setText(_data);

                _nlp0.put("enable",true);
                _nlp0.put("model","dialogue");
                _nlp0.put("outputSizeOption","small");

                _nlp1.put("enable",true);
                _nlp1.put("maxCount","30");

                _nlp2.put("enable",true);

                _nlp.put("summary",_nlp0);
                // _nlp.put("keywordExtraction",_nlp1);     // 키워드 추출하기
                // _nlp.put("sentimentAnalysis",_nlp2);     // default : true
                params.setNlpConfig(_nlp);
                String pp = new Gson().toJson(params);
                ProjectVO param = summary.getSummary(pp, "전체 요약문");
                if(StringUtils.isNotEmpty(param.getTitle())){
                    ProjectVO ridx = reportMapper.getReportIdx(paramVo);
                    Integer ridx0 = 0;
                    if(ridx==null){
                        Integer _seq = reportMapper.getReportSeq();
                        _seq = _seq+1;
                        String b1 = ("000"+_seq);
                        String RPID = "R" + b1.substring(b1.length()-4,b1.length());
                        paramVo.setReport_seq(_seq);
                        paramVo.setReport_id(RPID);
                        paramVo.setTitle(paramVo.getProject_name() + "_기본리포트");
                        ridx0 = reportMapper.savReport(paramVo);
                    }else{
                        paramVo.setIdx_report(ridx.getIdx_report());
                        ridx0 = 1;
                    }
                    if(ridx0==1){
                        paramVo.setTitle(param.getTitle());
                        paramVo.setSummary0(param.getSummary0());
                        reportMapper.saveReportData(paramVo);
                        statisticsService.createAPIUsage(paramVo, 1, paramVo.getSummary0()); //리포트 생성시 api사용량 누적
                        if(StringUtils.isNotEmpty(_token)){
                            _msg = "리포트가 생성되었습니다.";
                        }
                    }else{
                        _msg = "리포트 생성을 실패하였습니다.";
                    }
                }else{
                    _msg = "리포트 생성을 실패하였습니다.";
                }
            }
            if(StringUtils.isNotEmpty(_token)){
                // kafkaSender.send(_token, _msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            _msg = "리포트 생성을 실패하였습니다.";
            if(StringUtils.isNotEmpty(_token)){
                // kafkaSender.send(_token, _msg);
            }
        }
    }

    /**
     * 프로젝트 상세보기 리스트 만들기
     * @param rlist
     * @param prs0
     * @return ArrayList<ProjectViewVO>
     * @throws Exception
     */
    public List<ProjectViewVO> get_projectListView(List<ProjectViewVO> rlist, ProjectVO prs0) throws Exception {
        String _fpath = this.filepath + prs0.getFilepath() + prs0.getFilename();
        rlist = getCsvParse(rlist, _fpath);
        return rlist;
    }

    public List<ProjectViewVO> getCsvParse(List<ProjectViewVO> rlist, String _fPath) throws Exception {
        List<String[]> ers = excel.getCsvListData(_fPath);
        int j = 0;
        for(String[] _ers0 : ers){  // 줄
            if(j>0){
                int i = 0;
                ProjectViewVO _e = new ProjectViewVO();
                for(String _ers00 : _ers0){ // 컬럼
                    if(i==0){
                        _e.setChapter(_ers00.toString());
                    }
                    if(i==1){
                        _e.setSubchapter(_ers00.toString());
                    }
                    if(i==2){
                        _e.setQuestion(_ers00.toString());
                    }
                    if(i==3){
                        _e.setPerson(_ers00.toString());
                    }
                    if(i==4){
                        _e.setAnswer(_ers00.toString());
                    }
                    i++;
                }
                rlist.add(_e);
            }
            j++;
        }
        return rlist;
    }

    /**
     * 필터 적용 리포트 생성
     * @param filterVO
     * @throws Exception
     */
    @Async
    @Transactional
    public void list_reportfilter(String _token, ReportFilterDataVO filterVO) throws Exception{

        ProjectVO paramVo = new ProjectVO();
        paramVo.setIdx_project(filterVO.getIdx_project());
        paramVo.setIdx_project_job_projectid(filterVO.getIdx_project_job_projectid());
        paramVo.setIdx_user(filterVO.getIdx_user());
        paramVo.setIdx_filter(filterVO.getIdx_filter());

        int filter_op1 = filterVO.getFilter_op1();
        int filter_op2 = filterVO.getFilter_op2();
        String _msg = "";

        try {
            String[] ty1, ty2, ty3, ty4;
            String[] ty5 = null;

            ReportFilterKeywordVO rf = new ReportFilterKeywordVO();
            List<String[]> summ_keywords = new ArrayList<>();

            if(StringUtils.isNotEmpty(filterVO.getTp1())){ ty1 = filterVO.getTp1().split("//"); rf.setSpeaker(ty1); } else { return; } // 화자
            if(StringUtils.isNotEmpty(filterVO.getTp2())){ ty2 = filterVO.getTp2().split("//");} else { return; } // 챕터
            if(StringUtils.isNotEmpty(filterVO.getTp3())){ ty3 = filterVO.getTp3().split("//");} else { return; } // 서브챕터
            if(StringUtils.isNotEmpty(filterVO.getTp4())){ ty4 = filterVO.getTp4().split("//");} else { return; } // 질문
            if(StringUtils.isNotEmpty(filterVO.getTp5())){ ty5 = filterVO.getTp5().split("//"); rf.setDic_keywords(ty5);} // 사전키워드

            Boolean _isDicKey = StringUtils.isEmpty(filterVO.getTp5());

            List<ProjectVO> prs = reportMapper.getReportFileListOne(paramVo);
            int data_cnt = 0;

            if(filter_op1>0 && filter_op1<5){ //'화자+필터용 사전키워드'만 적용한 전체요약문
                for(ProjectVO prs0 : prs){
                    String _fpath = this.filepath + prs0.getFilepath() + prs0.getFilename();

                    List<SumtextVO> _data = new ArrayList<SumtextVO>();
                    Map<String, Object> _nlp = new HashMap<String, Object>();
                    Map<String, Object> _nlp0 = new HashMap<String, Object>();
                    Map<String, Object> _nlp1 = new HashMap<String, Object>();
                    Map<String, Object> _nlp2 = new HashMap<String, Object>();

                    SummaryVO params = new SummaryVO();
                    List<String[]> ers = excel.getCsvListData(_fpath);
                    int j = 0;
                    for(String[] _ers0 : ers){  // 줄
                        for (String ty1_f : ty1) {
                            if (j > 0) {
                                SumtextVO _elist = new SumtextVO();
                                if (_isDicKey && _ers0[3].equals(ty1_f)) {
                                    _elist.setSpeaker(_ers0[3].toString());
                                    _elist.setText(_ers0[4].toString());
                                    _data.add(_elist);
                                } else if(_ers0[3].equals(ty1_f)) {
                                    for (String dic_key : ty5) {
                                        if (_ers0[4].contains(dic_key)) {
                                            _elist.setSpeaker(_ers0[3].toString());
                                            _elist.setText(_ers0[4].toString());
                                            _data.add(_elist);
                                        }
                                    }
                                }
                            }
                        }
                        j++;
                    }
                    params.setText(_data);

                    _nlp0.put("enable",true);
                    _nlp0.put("model","dialogue");
                    _nlp0.put("outputSizeOption","small");
                    _nlp.put("summary",_nlp0);

                    _nlp1.put("enable",true);
                    _nlp1.put("maxCount",30);
                    _nlp.put("keywordExtraction",_nlp1);


                    _nlp2.put("enable",true);
                    _nlp.put("sentimentAnalysis",_nlp2);
                    params.setNlpConfig(_nlp);

                    String pp = new Gson().toJson(params);
                    ProjectVO param = summary.getSummary(pp, "전체 요약문");

                    if(StringUtils.isNotEmpty(param.getTitle())){
                        Integer _seq = reportMapper.getReportSeq();
                        _seq = _seq+1;
                        String b1 = ("000"+_seq);
                        String RPID = "R" + b1.substring(b1.length()-4,b1.length());
                        paramVo.setReport_seq(_seq);
                        paramVo.setReport_id(RPID);
                        paramVo.setFilepath(prs0.getFilepath());
                        paramVo.setFilename(prs0.getFilename());
                        paramVo.setTitle(filterVO.getReport_name());
                        Integer ridx0 = reportMapper.savReport(paramVo);

                        if(ridx0==1){
                            paramVo.setSummary0(param.getSummary0());
                            paramVo.setTitle(param.getTitle());
                            reportMapper.saveReportData(paramVo);
                            if(param.getSummary_keywords()!=null && param.getSummary_keywords().length>0){
                                summ_keywords.add(param.getSummary_keywords());
                            }
                            statisticsService.createAPIUsage(paramVo, 1, paramVo.getSummary0()); // api 사용량 집계(요약문)

                            if(StringUtils.isNotEmpty(_token)){
                                _msg = "필터 리포트가 생성되었습니다.";
                            }
                        }else{
                            _msg = "리포트 정보 저장을 실패하였습니다.";
                        }
                    }else{
                        _msg = "리포트 생성을 실패하였습니다.";
                    }

                    if(filter_op1>1){ //'화자+필터용 사전키워드'+챕터 요약문
                        for (String ty2_f : ty2) {
                            data_cnt = 0;
                            List<SumtextVO> chapter_data = new ArrayList<SumtextVO>();
                            SummaryVO params02 = new SummaryVO();
                            ers = excel.getCsvListData(_fpath);
                            j = 0;
                            for(String[] _ers0 : ers){
                                for (String ty1_f : ty1) {
                                    if(j>0){
                                        SumtextVO _elist = new SumtextVO();
                                        if(_isDicKey){
                                            if(_ers0[3].equals(ty1_f) && _ers0[0].equals(ty2_f)){
                                                _elist.setSpeaker(_ers0[3].toString());
                                                _elist.setText(_ers0[4].toString());
                                                chapter_data.add(_elist);
                                                data_cnt++;
                                            }
                                        } else if(_ers0[3].equals(ty1_f) && _ers0[0].equals(ty2_f)){
                                            for(String dic_key : ty5) {
                                                if(_ers0[4].contains(dic_key)){
                                                    _elist.setSpeaker(_ers0[3].toString());
                                                    _elist.setText(_ers0[4].toString());
                                                    chapter_data.add(_elist);
                                                    data_cnt++;
                                                }
                                            }
                                        }
                                    }
                                }
                                j++;
                            }
                            params02.setText(chapter_data);

                            _nlp = new HashMap<String, Object>();
                            _nlp0 = new HashMap<String, Object>();
                            _nlp1 = new HashMap<String, Object>();
                            _nlp2 = new HashMap<String, Object>();

                            if(data_cnt>0){
                                _nlp0.put("enable",true);
                                _nlp0.put("model","dialogue");
                                _nlp0.put("outputSizeOption","small");
                                _nlp.put("summary",_nlp0);

                                _nlp1.put("enable",true);
                                _nlp1.put("maxCount",30);
                                _nlp.put("keywordExtraction",_nlp1);

                                _nlp2.put("enable",true);
                                _nlp.put("sentimentAnalysis",_nlp2);

                                params02.setNlpConfig(_nlp);

                                pp = new Gson().toJson(params02);
                                param = summary.getSummary(pp, "챕터 [" + ty2_f + "] 요약문");

                                if(StringUtils.isNotEmpty(param.getTitle())){
                                    paramVo.setSummary0(param.getSummary0());
                                    paramVo.setTitle(param.getTitle());
                                    reportMapper.saveReportData(paramVo);
                                    if(param.getSummary_keywords()!=null && param.getSummary_keywords().length>0){
                                        summ_keywords.add(param.getSummary_keywords());
                                    }
                                    statisticsService.createAPIUsage(paramVo, 1, paramVo.getSummary0());

                                    if(StringUtils.isNotEmpty(_token)){
                                        _msg = "챕터 요약이 생성되었습니다.";
                                    }
                                }else{
                                    _msg = "챕터 요약 생성을 실패하였습니다.";
                                }
                            } else {
                                _msg = "선택한 챕터 필터를 다시 확인해주세요.";
                            }

                            if(filter_op1>2){
                                for (String ty3_f : ty3) { //'화자+필터용 사전키워드'+챕터+서브챕터 요약문
                                    data_cnt = 0;
                                    List<SumtextVO> subChap_data = new ArrayList<SumtextVO>();
                                    SummaryVO params03 = new SummaryVO();
                                    ers = excel.getCsvListData(_fpath);
                                    j = 0;
                                    for(String[] _ers0 : ers){
                                        for (String ty1_f : ty1) {
                                            if (j > 0) {
                                                SumtextVO _elist = new SumtextVO();
                                                if(_isDicKey){
                                                    if (_ers0[3].equals(ty1_f) && _ers0[0].equals(ty2_f) && _ers0[1].equals(ty3_f)) {
                                                        _elist.setSpeaker(_ers0[3].toString());
                                                        _elist.setText(_ers0[4].toString());
                                                        subChap_data.add(_elist);
                                                        data_cnt++;
                                                    }
                                                } else if (_ers0[3].equals(ty1_f) && _ers0[0].equals(ty2_f) && _ers0[1].equals(ty3_f)) {
                                                    for(String dic_key : ty5) {
                                                        if(_ers0[4].contains(dic_key)){
                                                            _elist.setSpeaker(_ers0[3].toString());
                                                            _elist.setText(_ers0[4].toString());
                                                            subChap_data.add(_elist);
                                                            data_cnt++;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        j++;
                                    }
                                    params03.setText(subChap_data);

                                    _nlp = new HashMap<String, Object>();
                                    _nlp0 = new HashMap<String, Object>();
                                    _nlp1 = new HashMap<String, Object>();
                                    _nlp2 = new HashMap<String, Object>();

                                    if(data_cnt>0){
                                        _nlp0.put("enable",true);
                                        _nlp0.put("model","dialogue");
                                        _nlp0.put("outputSizeOption","small");
                                        _nlp.put("summary",_nlp0);


                                        _nlp1.put("enable",true);
                                        _nlp1.put("maxCount",30);
                                        _nlp.put("keywordExtraction",_nlp1);


                                        _nlp2.put("enable",true);
                                        _nlp.put("sentimentAnalysis",_nlp2);

                                        params02.setNlpConfig(_nlp);

                                        //summaries 추출
                                        pp = new Gson().toJson(params03);
                                        param = summary.getSummary(pp, "서브챕터 [" + ty3_f + "] 요약문");

                                        if(StringUtils.isNotEmpty(param.getTitle())){
                                            paramVo.setSummary0(param.getSummary0());
                                            paramVo.setTitle(param.getTitle());
                                            reportMapper.saveReportData(paramVo);
                                            if(param.getSummary_keywords()!=null && param.getSummary_keywords().length>0){
                                                summ_keywords.add(param.getSummary_keywords());
                                            }
                                            statisticsService.createAPIUsage(paramVo, 1, paramVo.getSummary0());

                                            if(StringUtils.isNotEmpty(_token)){
                                                _msg = "서브챕터 요약이 생성되었습니다.";
                                            }
                                        }else{
                                            _msg = "서브챕터 요약 생성을 실패하였습니다.";
                                        }
                                    } else {
                                        _msg = "선택한 서브챕터 필터를 다시 확인해주세요.";
                                    }

                                    if(filter_op1>3){
                                        for (String ty4_f : ty4) { //'화자+필터용 사전키워드'+챕터+서브챕터+질문 요약문
                                            data_cnt = 0;
                                            List<SumtextVO> quest_data = new ArrayList<SumtextVO>();
                                            SummaryVO params04 = new SummaryVO();
                                            ers = excel.getCsvListData(_fpath);
                                            j = 0;
                                            for (String[] _ers0 : ers) {
                                                for (String ty1_f : ty1) {
                                                    if (j > 0) {
                                                        SumtextVO _elist = new SumtextVO();
                                                        if(_isDicKey){
                                                            if (_ers0[3].equals(ty1_f) && _ers0[0].equals(ty2_f) && _ers0[1].equals(ty3_f) && _ers0[2].equals(ty4_f)) {
                                                                _elist.setSpeaker(_ers0[3].toString());
                                                                _elist.setText(_ers0[4].toString());
                                                                quest_data.add(_elist);
                                                                data_cnt++;
                                                            }
                                                        } else if (_ers0[3].equals(ty1_f) && _ers0[0].equals(ty2_f) && _ers0[1].equals(ty3_f) && _ers0[2].equals(ty4_f)) {
                                                            for(String dic_key : ty5) {
                                                                if(_ers0[4].contains(dic_key)){
                                                                    _elist.setSpeaker(_ers0[3].toString());
                                                                    _elist.setText(_ers0[4].toString());
                                                                    quest_data.add(_elist);
                                                                    data_cnt++;
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                j++;
                                            }
                                            params04.setText(quest_data);

                                            _nlp = new HashMap<String, Object>();
                                            _nlp0 = new HashMap<String, Object>();
                                            _nlp1 = new HashMap<String, Object>();
                                            _nlp2 = new HashMap<String, Object>();

                                            if (data_cnt > 0) {
                                                _nlp0.put("enable", true);
                                                _nlp0.put("model", "dialogue");
                                                _nlp0.put("outputSizeOption", "small");
                                                _nlp.put("summary", _nlp0);


                                                _nlp1.put("enable", true);
                                                _nlp1.put("maxCount", 30);
                                                _nlp.put("keywordExtraction",_nlp1);


                                                _nlp2.put("enable", true);
                                                _nlp.put("sentimentAnalysis",_nlp2);

                                                params02.setNlpConfig(_nlp);

                                                //summaries 추출
                                                pp = new Gson().toJson(params04);
                                                param = summary.getSummary(pp, "질문 [" + ty4_f + "] 요약문");

                                                if(StringUtils.isNotEmpty(param.getTitle())){
                                                    paramVo.setSummary0(param.getSummary0());
                                                    paramVo.setTitle(param.getTitle());
                                                    reportMapper.saveReportData(paramVo);
                                                    if(param.getSummary_keywords()!=null && param.getSummary_keywords().length>0){
                                                        summ_keywords.add(param.getSummary_keywords());
                                                    }
                                                    statisticsService.createAPIUsage(paramVo, 1, paramVo.getSummary0());

                                                    if(StringUtils.isNotEmpty(_token)){
                                                        _msg = "질문 요약이 생성되었습니다.";
                                                    }
                                                }else{
                                                    _msg = "질문 요약 생성을 실패하였습니다.";
                                                }
                                            } else {
                                                _msg = "선택한 질문 필터를 다시 확인해주세요.";
                                            }
                                        } // 질문
                                    }
                                } // 서브챕터
                            }
                        } // 챕터
                    }
                    saveSummaryKeyword(paramVo, summ_keywords, filter_op2);
                } // 전체
            } else {
                _msg = "리포트 필터 옵션을 다시 선택해주세요.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            _msg = "리포트 생성을 실패하였습니다.";
            if(StringUtils.isNotEmpty(_token)){
                // kafkaSender.send(_token, _msg);
            }
        }
    }

    /**
     * 요약문 키워드 저장
     * @param param
     * @param s_keyword
     */
    private void saveSummaryKeyword(ProjectVO param, List<String[]> s_keyword, int op2) throws Exception {

        List<ProjectVO> prs = reportMapper.getReportFileListOne(param);

        if(s_keyword.size()>0){
            for(ProjectVO prs0 : prs) {
                String _fpath = this.filepath + prs0.getFilepath() + prs0.getFilename();

                ReportFilterKeywordVO reKeywords = new ReportFilterKeywordVO();
                reKeywords.setIdx_report(param.getIdx_report());
                reKeywords.setKeytype(0); // 임시작성. 명사형용사 향후 추가적용 필요

                int total_count = 0;

                for (String[] keywords : s_keyword) {
                    for (String key : keywords) {
                        if(op2==1 || (op2==0 && key.length()>1)){
                            reKeywords.setSum_keyword(key);
                            List<String[]> ers = excel.getCsvListData(_fpath);

                            int j = 0;
                            int count = 0;
                            for(String[] _ers0 : ers){
                                if (j > 0) {
                                    int index = 0;
                                    while (index >= 0) {
                                        index = _ers0[4].indexOf(key, index);
                                        if (index >= 0) {
                                            count++;
                                            index += key.length();
                                        }
                                    }
                                }
                                j++;
                            }

                            int _findkey = reportMapper.findReportKeyword(reKeywords);
                            reKeywords.setKeycount(count);

                            if(count>0 && _findkey==0){
                                int apiUse = reKeywords.getSum_keyword().length() * count;
                                reportMapper.createReportFilterData(reKeywords); // api 사용량 집계(요약문)
                                total_count += apiUse;
                            }
                        }
                    }
                }
                statisticsService.createAPIUsage(param, 2, total_count);
            }
        }
    }


    /**
     * 파일 병합 생성
     * @param paramVo
     * @return File
     * @throws Exception
     */
    public File merge_csv(ProjectVO paramVo) throws Exception{

        String path = "/report/" + paramVo.getJob_no() + "/";
        String fullpath = this.filepath + path;
        File fileDir = new File(fullpath);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }

        File mergeCsv = new File(fullpath, (paramVo.getProject_name() + ".csv"));
        OutputStreamWriter out = null;
        FileOutputStream fos = null;
        fos = new FileOutputStream(mergeCsv, true);
        out = new OutputStreamWriter(fos);
        out.write("\ufeff");

        String[] _mergeIdx = paramVo.getProject_merge_idx().split(",");

        int fileCnt = 0;
        for (String mergeIdx : _mergeIdx) {

            int lineCnt = 0;
            ProjectVO m0 = new ProjectVO();
            m0.setIdx_project(Integer.valueOf(mergeIdx));

            List<ProjectVO> prs = reportMapper.getReportFileListOne(m0);

            for (ProjectVO pr : prs) {
                String _fpath = this.filepath + pr.getFilepath() + pr.getFilename();

                List<String[]> ers = excel.getCsvListData(_fpath);

                for (String[] er : ers) {
                    if(fileCnt==0 || (fileCnt>0 && lineCnt>0)) {
                        for (String str00 : er) {
                            String aData = "";
                            aData = "\""+str00+"\",";
                            out.write(aData);
                        }
                        out.write("\r\n");
                    }
                    lineCnt++;
                }
            }
            fileCnt++;
        }

        if(out!=null){
            out.flush();
            out.close();
        }

        return mergeCsv;
    }

}
