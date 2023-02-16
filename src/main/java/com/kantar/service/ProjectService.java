package com.kantar.service;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.kantar.mapper.ProjectMapper;
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

@Service
public class ProjectService {
    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private Excel excel;

    @Autowired
    private Summary summary;

    @Autowired
    private KafkaSender kafkaSender;
    
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
            Map<String, Object> _kafka = new HashMap<String, Object>();
            _kafka.put("link","");

            ProjectVO rs0 = projectMapper.getProjectJobProjectid(paramVo);

            String _fpath = this.filepath + rs0.getFilepath() + rs0.getFilename();

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
                    paramVo.setD_count_total(1);
                    ridx0 = reportMapper.savReport(paramVo);
                }else{
                    paramVo.setIdx_report(ridx.getIdx_report());
                    ridx0 = 1;
                }
                if(ridx0==1){
                    paramVo.setTitle(param.getTitle());
                    paramVo.setSummary0(param.getSummary0());
                    reportMapper.saveReportData(paramVo);
                    reportMapper.updReportCountUp(paramVo);
                    statisticsService.createAPIUsage(paramVo, 1, paramVo.getSummary0()); //리포트 생성시 api사용량 누적
                    if(StringUtils.isNotEmpty(_token)){
                        _msg = "리포트가 생성되었습니다.";
                        _kafka.put("link","/report_detail/" + paramVo.getIdx_report());
                    }
                }else{
                    _msg = "리포트 생성을 실패하였습니다.";
                }
            }else{
                _msg = "리포트 생성을 실패하였습니다.";
            }
            if(StringUtils.isNotEmpty(_token)){
                _kafka.put("msg",_msg);
                _kafka.put("roomId",_token);
                kafkaSender.send("kantar", new Gson().toJson(_kafka));
            }
        } catch (Exception e) {
            e.printStackTrace();
            _msg = "리포트 생성을 실패하였습니다.";
            if(StringUtils.isNotEmpty(_token)){
                Map<String, Object> _data2 = new HashMap<String, Object>();
                _data2.put("link","");
                _data2.put("msg",_msg);
                _data2.put("roomId",_token);
                kafkaSender.send("kantar", new Gson().toJson(_data2));
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
        if(ers != null && ers.size() > 0){
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
        }
        return rlist;
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

        for (String mergeIdx : _mergeIdx) {
            int lineCnt = 0;
            ProjectVO param = new ProjectVO();
            param.setIdx_project_job_projectid(Integer.valueOf(mergeIdx));
            ProjectVO rs0 = projectMapper.getProjectJobProjectid(param);
            String _fpath = this.filepath + rs0.getFilepath() + rs0.getFilename();

            List<String[]> ers = excel.getCsvListData(_fpath);

            for (String[] er : ers) {
                if(lineCnt>0) {
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

        if(out!=null){
            out.flush();
            out.close();
        }
        return mergeCsv;
    }

    /**
     * 필터 적용 리포트 생성
     * @param reportVO
     * @throws Exception
     */
    @Async
    @Transactional
    public void list_reportfilter(String _token, ReportVO reportVO) throws Exception{
        Map<String, Object> _kafka = new HashMap<String, Object>();
        _kafka.put("link","");

        ProjectVO paramVo = new ProjectVO();
        paramVo.setIdx_project(reportVO.getIdx_project());
        paramVo.setIdx_project_job_projectid(reportVO.getIdx_project_job_projectid());
        paramVo.setIdx_user(reportVO.getIdx_user());
        paramVo.setIdx_filter(reportVO.getIdx_filter());
        String _msg = "";

        try {
            //필터링 셋팅
            String[] _speakers = (StringUtils.isNotEmpty(reportVO.getTp1())) ? reportVO.getTp1().split("//") : null;
            String[] _chapters = (StringUtils.isNotEmpty(reportVO.getTp2())) ? reportVO.getTp2().split("//") : null;
            String[] _subs = (StringUtils.isNotEmpty(reportVO.getTp3())) ? reportVO.getTp3().split("//") : null;
            String[] _questions = (StringUtils.isNotEmpty(reportVO.getTp4())) ? reportVO.getTp4().split("//") : null;
            String[] _keywords = (StringUtils.isNotEmpty(reportVO.getTp5())) ? reportVO.getTp5().split("//") : null;

            //파일 셋팅
            ProjectVO rs0 = projectMapper.getProjectJobProjectid(paramVo);
            String _fpath = this.filepath + rs0.getFilepath() + rs0.getFilename();
            List<String[]> ers = excel.getCsvListData(_fpath);

            //리스트 및 생성시 필요변수 셋틍
            List<SumtextVO> _data0 = new ArrayList<SumtextVO>();
            List<ReportVO> _data10 = new ArrayList<ReportVO>();
            List<String[]> summ_keywords = new ArrayList<>();
            List<String[]> summ_adjectives = new ArrayList<>();
            Integer _totalCount = 0; // 요약문 생성수
            Integer _nowCount = 0;
            String pp;
            List<ReportMetaDataVO> _metalist = new ArrayList<ReportMetaDataVO>();

            //요약문 설정 셋팅
            Map<String, Object> _nlp = new HashMap<String, Object>();
            Map<String, Object> _nlp0 = new HashMap<String, Object>();
            Map<String, Object> _nlp1 = new HashMap<String, Object>();
            Map<String, Object> _nlp2 = new HashMap<String, Object>();
            _nlp0.put("enable",true);
            _nlp0.put("model","dialogue");
            _nlp0.put("outputSizeOption","small");
            _nlp.put("summary",_nlp0);
            _nlp1.put("maxCount",30);
            _nlp1.put("enable",true);
            if(reportVO.getRfil5()==1){
                _nlp1.put("extractAdjectives",false);
            }
            if(reportVO.getRfil5()==2 || reportVO.getRfil5()==3){
                _nlp1.put("extractAdjectives",true);
            }
            _nlp.put("keywordExtraction",_nlp1);
            _nlp2.put("enable",true);
            _nlp.put("sentimentAnalysis",_nlp2);

            int j = 0;
            for(String[] _ers0 : ers) {  // 전체 리스트
                if (j > 0) {
                    SumtextVO _elist = new SumtextVO();
                    _elist.setSpeaker(_ers0[3].toString());
                    _elist.setText(_ers0[4].toString());
                    _data0.add(_elist);
                    ReportVO _rlist = new ReportVO();
                    _rlist.setTp1(_ers0[3].toString()); // 화자
                    _rlist.setTp2(_ers0[0].toString()); // 챕터
                    _rlist.setTp3(_ers0[1].toString()); // 서브챕터
                    _rlist.setTp4(_ers0[2].toString()); // 질문
                    _rlist.setTp5(_ers0[4].toString()); // 답변
                    _data10.add(_rlist);
                }
                j++;
            }

            List<SumtextVO> _data9 = new ArrayList<SumtextVO>();
            List<ReportVO> _data99 = new ArrayList<ReportVO>();
            if(StringUtils.isNotEmpty(reportVO.getTp1())) { // 화자
                for (String s : _speakers) {
                    for (ReportVO data : _data10) {
                        if(data.getTp1().equals(s)) {
                            ReportVO _r = new ReportVO();
                            _r.setTp1(data.getTp1());
                            _r.setTp2(data.getTp2());
                            _r.setTp3(data.getTp3());
                            _r.setTp4(data.getTp4());
                            _r.setTp5(data.getTp5());
                            _data99.add(_r);
                            SumtextVO _s = new SumtextVO();
                            _s.setSpeaker(data.getTp1());
                            _s.setText(_r.getTp5());
                            _data9.add(_s);
                        }
                    }
                }
                _data0 = _data9;
                _data10 = _data99;
            }

            _data9 = new ArrayList<SumtextVO>();
            _data99 = new ArrayList<ReportVO>();
            if(StringUtils.isNotEmpty(reportVO.getTp5())) { // 키워드
                for (String k : _keywords) {
                    for (ReportVO data : _data10) {
                        if(data.getTp5().contains(k)) {
                            ReportVO _r = new ReportVO();
                            _r.setTp1(data.getTp1());
                            _r.setTp2(data.getTp2());
                            _r.setTp3(data.getTp3());
                            _r.setTp4(data.getTp4());
                            _r.setTp5(data.getTp5());
                            _data99.add(_r);
                            SumtextVO _s = new SumtextVO();
                            _s.setSpeaker(data.getTp1());
                            _s.setText(_r.getTp5());
                            _data9.add(_s);
                        }
                    }
                }
                _data0 = _data9;
                _data10 = _data99;
            }

            //totalCount 집계
            Integer dataCnt = 0;
            if(reportVO.getRfil1()==1 && StringUtils.isNotEmpty(reportVO.getTp2())) {
                for (String c : _chapters) {
                    for (ReportVO data : _data10) {
                        if(data.getTp2().equals(c)) { dataCnt++; }
                    } if(dataCnt>0){ _totalCount++; }
                }
            }
            if(reportVO.getRfil1()==1 && StringUtils.isEmpty(reportVO.getTp2())) {
                Map<String, List<ReportVO>> grouped = new HashMap<>();
                for (ReportVO _d : _data10) {
                    String key = _d.getTp2();
                    if (!grouped.containsKey(key)) {
                        grouped.put(key, new ArrayList<>());
                    }
                    grouped.get(key).add(_d);
                }
                for (String c : grouped.keySet()) {
                    List<ReportVO> _chapReport = grouped.get(c);
                    for (ReportVO data : _chapReport) { dataCnt++; }
                    if(dataCnt>0){ _totalCount++; }
                }
            }
            if(reportVO.getRfil0()==1){ // 전체 요약문
                _totalCount++;
            }
            if(reportVO.getRfil2()==1 && StringUtils.isNotEmpty(reportVO.getTp3())) {
                for (String s : _subs) {
                    for (ReportVO data : _data10) {
                        if(data.getTp3().equals(s)) { dataCnt++; }
                    } if(dataCnt>0){ _totalCount++; }
                }
            }
            if(reportVO.getRfil3()==1 && StringUtils.isNotEmpty(reportVO.getTp4())) {
                for (String q : _questions) {
                    for (ReportVO data : _data10) {
                        if(data.getTp4().equals(q)) { dataCnt++; }
                    } if(dataCnt>0){ _totalCount++; }
                }
            }

            if(_totalCount != 0){ //리포트 생성
                Integer _seq = reportMapper.getReportSeq();
                _seq = _seq+1;
                String b1 = ("000"+_seq);
                String RPID = "R" + b1.substring(b1.length()-4,b1.length());
                paramVo.setReport_seq(_seq);
                paramVo.setReport_id(RPID);
                paramVo.setTitle(reportVO.getTitle());
                paramVo.setD_count_total(_totalCount); // 요약문 데이터 총 갯수
                Integer ridx0 = reportMapper.savReport(paramVo);
            }

            _data9 = new ArrayList<SumtextVO>();
            _data99 = new ArrayList<ReportVO>();
            if(reportVO.getRfil1()==1 && StringUtils.isNotEmpty(reportVO.getTp2())) { // 챕터 선택
                for (String c : _chapters) {
                    for (ReportVO data : _data10) {
                        if(data.getTp2().equals(c)) {
                            ReportVO _r = new ReportVO();
                            _r.setTp1(data.getTp1()); // 화자
                            _r.setTp2(data.getTp2()); // 챕터
                            _r.setTp3(data.getTp3()); // 서브챕터
                            _r.setTp4(data.getTp4()); // 질문
                            _r.setTp5(data.getTp5()); // 답변
                            _data99.add(_r);
                            SumtextVO _s = new SumtextVO();
                            _s.setSpeaker(data.getTp1());
                            _s.setText(_r.getTp5());
                            _data9.add(_s);
                            ReportMetaDataVO _meta = new ReportMetaDataVO();
                            _meta.setSpeaker(data.getTp1());
                            _meta.setChapter(data.getTp2());
                            _meta.setLength(data.getTp5().length());
                            _meta.setCnt(1);
                            _metalist.add(_meta);
                        }
                    }
                    SummaryVO params = new SummaryVO();
                    params.setText(_data9);
                    params.setNlpConfig(_nlp);
                    pp = new Gson().toJson(params);
                    ProjectVO param = summary.getSummary(pp, "챕터 ["+c+"] 요약문");

                    if(StringUtils.isNotEmpty(param.getTitle())) {
                        paramVo.setSummary0(param.getSummary0());
                        paramVo.setTitle(param.getTitle());
                        reportMapper.saveReportData(paramVo);
                        reportMapper.updReportCountUp(paramVo);
                        _nowCount++;
                        if(reportVO.getRfil5()==1 || reportVO.getRfil5()==3){
                            summ_keywords.add(param.getSummary_keywords()); // 추출된 명사 있으면 명사 리스트에 저장
                        }
                        if(reportVO.getRfil5()==2 || reportVO.getRfil5()==3){
                            summ_adjectives.add(param.getSummary_adjectives()); // 추출된 형용사 있으면 형용사 리스트에 저장
                        }
                        statisticsService.createAPIUsage(paramVo, 1, paramVo.getSummary0()); // api 사용량 집계(요약문)
                        if (StringUtils.isNotEmpty(_token)) {
                            _msg = "챕터 리포트가 생성되었습니다.";
                        }
                    }
                }
            }

            if(reportVO.getRfil1()==1 && StringUtils.isEmpty(reportVO.getTp2())) { // 챕터 미선택
                Map<String, List<ReportVO>> grouped = new HashMap<>();
                for (ReportVO _d : _data10) {
                    String key = _d.getTp2();
                    if (!grouped.containsKey(key)) {
                        grouped.put(key, new ArrayList<>());
                    }
                    grouped.get(key).add(_d);
                }

                for (String c : grouped.keySet()) {
                    List<ReportVO> _chapReport = grouped.get(c);
                    for (ReportVO data : _chapReport) {
                        ReportVO _r = new ReportVO();
                        _r.setTp1(data.getTp1()); // 화자
                        _r.setTp2(data.getTp2()); // 챕터
                        _r.setTp3(data.getTp3()); // 서브챕터
                        _r.setTp4(data.getTp4()); // 질문
                        _r.setTp5(data.getTp5()); // 답변
                        _data99.add(_r);
                        SumtextVO _s = new SumtextVO();
                        _s.setSpeaker(data.getTp1());
                        _s.setText(_r.getTp5());
                        _data9.add(_s);
                        ReportMetaDataVO _meta = new ReportMetaDataVO();
                        _meta.setSpeaker(data.getTp1());
                        _meta.setChapter(data.getTp2());
                        _meta.setLength(data.getTp5().length());
                        _meta.setCnt(1);
                        _metalist.add(_meta);
                    }
                    // 챕터 요약문 생성
                    SummaryVO params = new SummaryVO();
                    params.setText(_data9);
                    params.setNlpConfig(_nlp);
                    pp = new Gson().toJson(params);
                    ProjectVO param = summary.getSummary(pp, "챕터 ["+c+"] 요약문");

                    if(StringUtils.isNotEmpty(param.getTitle())) {
                        paramVo.setSummary0(param.getSummary0());
                        paramVo.setTitle(param.getTitle());
                        reportMapper.saveReportData(paramVo);
                        reportMapper.updReportCountUp(paramVo);
                        _nowCount++;
                        if(reportVO.getRfil5()==1 || reportVO.getRfil5()==3){
                            summ_keywords.add(param.getSummary_keywords()); // 추출된 명사 있으면 명사 리스트에 저장
                        }
                        if(reportVO.getRfil5()==2 || reportVO.getRfil5()==3){
                            summ_adjectives.add(param.getSummary_adjectives()); // 추출된 형용사 있으면 형용사 리스트에 저장
                        }
                        statisticsService.createAPIUsage(paramVo, 1, paramVo.getSummary0()); // api 사용량 집계(요약문)
                        if (StringUtils.isNotEmpty(_token)) {
                            _msg = "챕터 리포트가 생성되었습니다.";
                        }
                    }
                }
            }

            if(reportVO.getRfil0()==1){ // 전체 요약문
                SummaryVO params = new SummaryVO();
                params.setText(_data0);
                params.setNlpConfig(_nlp);
                pp = new Gson().toJson(params);
                ProjectVO param = summary.getSummary(pp, "전체 요약문");

                if(StringUtils.isNotEmpty(param.getTitle())){
                    paramVo.setSummary0(param.getSummary0());
                    paramVo.setTitle(param.getTitle());
                    reportMapper.saveReportData(paramVo);
                    reportMapper.updReportCountUp(paramVo);
                    _nowCount++;
                    if(reportVO.getRfil5()==1 || reportVO.getRfil5()==3){
                        summ_keywords.add(param.getSummary_keywords()); // 추출된 명사 있으면 명사 리스트에 저장
                    }
                    if(reportVO.getRfil5()==2 || reportVO.getRfil5()==3){
                        summ_adjectives.add(param.getSummary_adjectives()); // 추출된 형용사 있으면 형용사 리스트에 저장
                    }
                    statisticsService.createAPIUsage(paramVo, 1, paramVo.getSummary0());
                    if(StringUtils.isNotEmpty(_token)){
                        _msg = "전체 요약이 생성되었습니다.";
                    }
                    _metalist = new ArrayList<>();
                    for (ReportVO data : _data10) {
                        ReportMetaDataVO _meta = new ReportMetaDataVO();
                        _meta.setSpeaker(data.getTp1());
                        _meta.setChapter(data.getTp2());
                        _meta.setLength(data.getTp5().length());
                        _meta.setCnt(1);
                        _metalist.add(_meta);
                    }
                }else{ _msg = "전체 요약 생성을 실패하였습니다."; }
            }

            _data9 = new ArrayList<SumtextVO>();
            _data99 = new ArrayList<ReportVO>();
            if(reportVO.getRfil2()==1 && StringUtils.isNotEmpty(reportVO.getTp3())) { // 서브챕터 요약문
                for (String s : _subs) {
                    for (ReportVO data : _data10) {
                        if(data.getTp3().equals(s)) {
                            ReportVO _r = new ReportVO();
                            _r.setTp1(data.getTp1()); // 화자
                            _r.setTp2(data.getTp2()); // 챕터
                            _r.setTp3(data.getTp3()); // 서브챕터
                            _r.setTp4(data.getTp4()); // 질문
                            _r.setTp5(data.getTp5()); // 답변
                            _data99.add(_r);

                            SumtextVO _s = new SumtextVO();
                            _s.setSpeaker(data.getTp1());
                            _s.setText(_r.getTp5());
                            _data9.add(_s);
                        }
                    }
                    SummaryVO params = new SummaryVO();
                    params.setText(_data9);
                    params.setNlpConfig(_nlp);
                    pp = new Gson().toJson(params);
                    ProjectVO param = summary.getSummary(pp, "서브챕터 ["+s+"] 요약문");

                    if(StringUtils.isNotEmpty(param.getTitle())){
                        paramVo.setSummary0(param.getSummary0());
                        paramVo.setTitle(param.getTitle());
                        reportMapper.saveReportData(paramVo);
                        reportMapper.updReportCountUp(paramVo);
                        _nowCount++;
                        if(reportVO.getRfil5()==1 || reportVO.getRfil5()==3){
                            summ_keywords.add(param.getSummary_keywords()); // 추출된 명사 있으면 명사 리스트에 저장
                        }
                        if(reportVO.getRfil5()==2 || reportVO.getRfil5()==3){
                            summ_adjectives.add(param.getSummary_adjectives()); // 추출된 형용사 있으면 형용사 리스트에 저장
                        }
                        statisticsService.createAPIUsage(paramVo, 1, paramVo.getSummary0());

                        if(StringUtils.isNotEmpty(_token)){
                            _msg = "서브챕터 요약이 생성되었습니다.";
                        }
                    }else{ _msg = "서브챕터 요약 생성을 실패하였습니다."; }
                }
            }

            _data9 = new ArrayList<SumtextVO>();
            _data99 = new ArrayList<ReportVO>();
            if(reportVO.getRfil3()==1 && StringUtils.isNotEmpty(reportVO.getTp4())) { // 질문 요약문
                for (String q : _questions) {
                    for (ReportVO data : _data10) {
                        if(data.getTp4().equals(q)) {
                            ReportVO _r = new ReportVO();
                            _r.setTp1(data.getTp1()); // 화자
                            _r.setTp2(data.getTp2()); // 챕터
                            _r.setTp3(data.getTp3()); // 서브챕터
                            _r.setTp4(data.getTp4()); // 질문
                            _r.setTp5(data.getTp5()); // 답변
                            _data99.add(_r);

                            SumtextVO _s = new SumtextVO();
                            _s.setSpeaker(data.getTp1());
                            _s.setText(_r.getTp5());
                            _data9.add(_s);
                        }
                    }
                    SummaryVO params = new SummaryVO();
                    params.setText(_data9);
                    params.setNlpConfig(_nlp);
                    pp = new Gson().toJson(params);
                    ProjectVO param = summary.getSummary(pp, "질문 ["+q+"] 요약문");

                    if(StringUtils.isNotEmpty(param.getTitle())){
                        paramVo.setSummary0(param.getSummary0());
                        paramVo.setTitle(param.getTitle());
                        reportMapper.saveReportData(paramVo);
                        reportMapper.updReportCountUp(paramVo);
                        _nowCount++;
                        if(reportVO.getRfil5()==1 || reportVO.getRfil5()==3){
                            summ_keywords.add(param.getSummary_keywords()); // 추출된 명사 있으면 명사 리스트에 저장
                        }
                        if(reportVO.getRfil5()==2 || reportVO.getRfil5()==3){
                            summ_adjectives.add(param.getSummary_adjectives()); // 추출된 형용사 있으면 형용사 리스트에 저장
                        }
                        statisticsService.createAPIUsage(paramVo, 1, paramVo.getSummary0());

                        if(StringUtils.isNotEmpty(_token)){
                            _msg = "질문 요약이 생성되었습니다.";
                        }
                    }else{ _msg = "질문 요약 생성을 실패하였습니다."; }
                }
            }

            savMetadata(paramVo.getIdx_report(), _metalist); // 메타데이터 집계
            reportVO.setIdx_report(paramVo.getIdx_report());

            Map _km = new HashMap();
            saveSummaryKeyword(_data10, summ_keywords, summ_adjectives, reportVO); //api 사용량 집계

            if(paramVo.getIdx_report()>0 && _totalCount==_nowCount){
                _msg = "리포트가 생성되었습니다.";
                _kafka.put("link","/report_detail/" + paramVo.getIdx_report());
            }

            if(StringUtils.isNotEmpty(_token)){
                _kafka.put("msg",_msg);
                _kafka.put("roomId",_token);
                kafkaSender.send("kantar", new Gson().toJson(_kafka));
            }

            } catch (Exception e) {
            e.printStackTrace();
            _msg = "리포트 생성을 실패하였습니다.";
            if(StringUtils.isNotEmpty(_token)){
                Map<String, Object> _data2 = new HashMap<String, Object>();
                _data2.put("link","");
                _data2.put("msg",_msg);
                _data2.put("roomId",_token);
                kafkaSender.send("kantar", new Gson().toJson(_data2));
            }
        }
    }

    /**
     * 메타데이터 저장
     * @param idx
     * @param metalist
     */
    private void savMetadata(int idx, List<ReportMetaDataVO> metalist) throws Exception {
        for (ReportMetaDataVO md : metalist) {
            md.setIdx_report(idx);
            int _isSave = reportMapper.getMetadataInfoByIdx(md);
            if(_isSave==0){
                reportMapper.insertMetadata(md);
            } else if (_isSave>0) {
                reportMapper.updateMetadataCnt(md);
            }
        }
    }

    /**
     * 요약문 키워드 저장
     * @param allList
     * @param s_keyword
     * @param s_adjectives
     * @param reportVO
     */
    private void saveSummaryKeyword(List<ReportVO> allList, List<String[]> s_keyword, List<String[]> s_adjectives, ReportVO reportVO) throws Exception {

        ReportFilterKeywordVO reKeywords = new ReportFilterKeywordVO();
        reKeywords.setIdx_report(reportVO.getIdx_report());

        int total_count = 0;

        for (String[] keywords : s_keyword) {
            if(keywords!=null && keywords.length>0){
                for (String key : keywords) {
                    if(reportVO.getRfil4()==0 || (reportVO.getRfil4()==1 && key.length()>1)){
                        reKeywords.setSum_keyword(key);
                        reKeywords.setKeytype(1);
                        int count = 0;
                        for (ReportVO _r : allList) {
                            int index = 0;
                            while (index >= 0) {
                                index = _r.getTp5().indexOf(key, index);
                                if (index >= 0) {
                                    count++;
                                    index += key.length();
                                }
                            }
                        }
                        int _findkey = reportMapper.findReportKeyword(reKeywords);
                        reKeywords.setKeycount(count);

                        if(count>0 && _findkey==0){
                            int apiUse = reKeywords.getSum_keyword().length() * count;
                            reportMapper.createReportFilterData(reKeywords); // 키워드 집계
                            total_count += apiUse;
                        }
                    }
                }
            }
        }

        for (String[] adjectives : s_adjectives) { //형용사 집계
            if(adjectives!=null && adjectives.length>0){
                for (String adj : adjectives) {
                    if(reportVO.getRfil4()==0 || (reportVO.getRfil4()==1 && adj.length()>1)){
                        reKeywords.setSum_keyword(adj);
                        reKeywords.setKeytype(2);
                        int count = 0;
                        for (ReportVO _r : allList) {
                            int index = 0;
                            while (index >= 0) {
                                index = _r.getTp5().indexOf(adj, index);
                                if (index >= 0) {
                                    count++;
                                    index += adj.length();
                                }
                            }
                        }
                        int _findkey = reportMapper.findReportKeyword(reKeywords);
                        reKeywords.setKeycount(count);

                        if(count>0 && _findkey==0){
                            int apiUse = reKeywords.getSum_keyword().length() * count;
                            reportMapper.createReportFilterData(reKeywords); // 키워드 집계
                            total_count += apiUse;
                        }
                    }
                }
            }
        }

        ProjectVO param = new ProjectVO();
        param.setIdx_report(reKeywords.getIdx_report());
        param.setIdx_user(reportVO.getIdx_user());
        statisticsService.createAPIUsage(param, 2, total_count); // api 사용량 집계(키워드)
    }

}