package com.kantar.service;

import com.google.gson.Gson;
import com.kantar.mapper.FilterMapper;
import com.kantar.mapper.ProjectMapper;
import com.kantar.mapper.WordCloudMapper;
import com.kantar.util.Excel;
import com.kantar.util.Summary;
import com.kantar.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WordCloudService {

    @Autowired
    private WordCloudMapper wordCloudMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private Summary summary;
    @Autowired
    private Excel excel;

    @Value("${file.upload-dir}")
    public String filepath;

    @Autowired
    private KafkaSender kafkaSender;

    public Integer create_Filter(FilterVO paramVo, String _data) throws Exception {
        String[] _data1 = _data.split("//");
        Map<String, Object> _sptd = new HashMap<String, Object>();
        ArrayList<Object> _sptd0 = new ArrayList<Object>();
        for(String _data0 : _data1){
            FilterDataVO param = new FilterDataVO();
            param.setIdx_filter(paramVo.getIdx_filter());
            param.setFilter_type(paramVo.getFilter_type());
            param.setFilter_data(_data0);
            _sptd0.add(param);
        }
        _sptd.put("list",_sptd0);
        Integer rs1 = wordCloudMapper.createWordCloudFilterData(_sptd);
        if(rs1 == 0){
            wordCloudMapper.delWordCloudFilter(paramVo);
            return 0;
        }else{
            return 1;
        }
    }

    /**
     * 조건지정 리포트용 필터 생성
     */
    public Integer createWordCloudFilter(WordCloudVO wc) throws Exception {
        Integer rs0 = 0;

        FilterVO paramVo = new FilterVO();
        paramVo.setFilter_title(wc.getTitle() + " filter");
        paramVo.setIdx_user(wc.getIdx_user());
        paramVo.setIdx_project_job_projectid(wc.getIdx_project_job_projectid());

        rs0 = wordCloudMapper.createWordCloudFilter(paramVo);

        if(rs0 == 1){
            if(StringUtils.isNotEmpty(wc.getTp1())){
                paramVo.setFilter_type(1);
                Integer rs1 = create_Filter(paramVo, wc.getTp1());
                if(rs1 == 0){
                    wordCloudMapper.delWordCloudFilter(paramVo);
                }
            }
            if(StringUtils.isNotEmpty(wc.getTp2())){
                paramVo.setFilter_type(2);
                Integer rs1 = create_Filter(paramVo, wc.getTp2());
                if(rs1 == 0){
                    wordCloudMapper.delWordCloudFilter(paramVo);
                }
            }
            if(StringUtils.isNotEmpty(wc.getTp3())){
                paramVo.setFilter_type(3);
                Integer rs1 = create_Filter(paramVo, wc.getTp3());
                if(rs1 == 0){
                    wordCloudMapper.delWordCloudFilter(paramVo);
                }
            }
            if(StringUtils.isNotEmpty(wc.getTp4())){
                paramVo.setFilter_type(4);
                Integer rs1 = create_Filter(paramVo, wc.getTp4());
                if(rs1 == 0){
                    wordCloudMapper.delWordCloudFilter(paramVo);
                }
            }
            if(StringUtils.isNotEmpty(wc.getTp5())){
                paramVo.setFilter_type(5);
                Integer rs1 = create_Filter(paramVo, wc.getTp5());
                if(rs1 == 0){
                    wordCloudMapper.delWordCloudFilter(paramVo);
                }
            }
        }
        return paramVo.getIdx_filter();
    }



    /**
     * 워드 클라우드 데이터 생성
     */
    @Async
    @Transactional
    public void createWordCloud(String _token, WordCloudVO wc) throws Exception{

        ProjectVO paramVo = new ProjectVO();
        paramVo.setIdx_project(wc.getIdx_project());
        paramVo.setIdx_project_job_projectid(wc.getIdx_project_job_projectid());
        paramVo.setIdx_user(wc.getIdx_user());
        paramVo.setIdx_filter(wc.getIdx_word_filter());
        String _msg = "";

        try {
            Map<String, Object> _kafka = new HashMap<String, Object>();
            _kafka.put("link","");

            //필터링 셋팅
            String[] _speakers = (StringUtils.isNotEmpty(wc.getTp1())) ? wc.getTp1().split("//") : null;
            String[] _chapters = (StringUtils.isNotEmpty(wc.getTp2())) ? wc.getTp2().split("//") : null;
            String[] _subs = (StringUtils.isNotEmpty(wc.getTp3())) ? wc.getTp3().split("//") : null;
            String[] _questions = (StringUtils.isNotEmpty(wc.getTp4())) ? wc.getTp4().split("//") : null;
            String[] _keywords = (StringUtils.isNotEmpty(wc.getTp5())) ? wc.getTp5().split("//") : null;

            //파일 셋팅
            ProjectVO rs0 = projectMapper.getProjectJobProjectid(paramVo);
            String _fpath = this.filepath + rs0.getFilepath() + rs0.getFilename();
            List<String[]> ers = excel.getCsvListData(_fpath);

            if(ers.size() > 7000){
                _msg = "7000줄 이상은 처리할 수 없습니다.";
            }else{
                //리스트 및 생성시 필요변수 셋틍
                List<SumtextVO> _data0 = new ArrayList<SumtextVO>();
                List<ReportVO> _data10 = new ArrayList<ReportVO>();
                List<String[]> summ_keywords = new ArrayList<>();
                List<String[]> summ_adjectives = new ArrayList<>();
                String pp;

                //요약문 설정 셋팅
                Map<String, Object> _nlp = new HashMap<String, Object>();
                Map<String, Object> _nlp0 = new HashMap<String, Object>();
                Map<String, Object> _nlp1 = new HashMap<String, Object>();
                Map<String, Object> _nlp2 = new HashMap<String, Object>();
                _nlp0.put("enable",true);
                _nlp0.put("model","dialogue");
                _nlp0.put("outputSizeOption","small");
                _nlp.put("summary",_nlp0);
                _nlp1.put("enable",true);
                _nlp1.put("maxCount",30);
                if(wc.getKeyType()==1){
                    _nlp1.put("extractAdjectives",false);
                }
                if(wc.getKeyType()==2 || wc.getKeyType()==3){
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
                if(StringUtils.isNotEmpty(wc.getTp1())) { // 화자
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
                if(StringUtils.isNotEmpty(wc.getTp5())) { // 키워드
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

                _data9 = new ArrayList<SumtextVO>();
                _data99 = new ArrayList<ReportVO>();
                if(StringUtils.isNotEmpty(wc.getTp2())) { // 챕터 선택
                    for (String c : _chapters) {
                        for (ReportVO data : _data10) {
                            if(data.getTp2().equals(c)) {
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
                if(StringUtils.isNotEmpty(wc.getTp3())) { // 서브챕터 선택
                    for (String c : _subs) {
                        for (ReportVO data : _data10) {
                            if(data.getTp3().equals(c)) {
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
                if(StringUtils.isNotEmpty(wc.getTp4())) { // 질문 선택
                    for (String c : _questions) {
                        for (ReportVO data : _data10) {
                            if(data.getTp4().equals(c)) {
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

                SummaryVO params = new SummaryVO();
                params.setText(_data0);
                params.setNlpConfig(_nlp);
                pp = new Gson().toJson(params);
                ProjectVO param = summary.getSummary(pp, wc.getTitle());

                if(param.getDagloerr() == 0){
                    if((param.getSummary_keywords() != null && param.getSummary_keywords().length > 0) || (param.getSummary_adjectives() != null && param.getSummary_adjectives().length > 0)) {
                        int _r = wordCloudMapper.saveWordCloud(wc);
                        if(_r==1){
                            summ_keywords.add(param.getSummary_keywords()); // 추출된 명사 있으면 명사 리스트에 저장
                            if(wc.getKeyType()==1 || wc.getKeyType()==3){
                                summ_keywords.add(param.getSummary_keywords()); // 추출된 명사 있으면 명사 리스트에 저장
                            }
                            if(wc.getKeyType()==2 || wc.getKeyType()==3){
                                summ_adjectives.add(param.getSummary_adjectives()); // 추출된 형용사 있으면 형용사 리스트에 저장
                            }
                            saveWordCloudKetword(_data10, summ_keywords, summ_adjectives, wc);
                            _msg = "워드클라우드 생성이 완료되었습니다.";
                            _kafka.put("link","/wordcloud_view/" + wc.getIdx_wordcloud());
                        } else {
                            _msg = "워드 클라우드 생성에 실패했습니다.";
                        }
                    } else {
                        _msg = "추출된 키워드가 없어서 워드 클라우드 생성을 중단합니다.";
                    }
                }else{
                    _msg = "daglo 서비스 오류로 워드 클라우드 생성을 중단합니다.";
                }
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
     * 워드클라우드 키워드 저장
     */
    private void saveWordCloudKetword(List<ReportVO> allList, List<String[]> s_keyword, List<String[]> s_adjectives, WordCloudVO wc) throws Exception {

        ReportFilterKeywordVO reKeywords = new ReportFilterKeywordVO();
        reKeywords.setIdx_report(wc.getIdx_wordcloud());

        for (String[] keywords : s_keyword) {
            if(keywords!=null && keywords.length>0){
                for (String key : keywords) {
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
                    int _findkey = wordCloudMapper.findWordCloudKeyword(reKeywords);
                    reKeywords.setKeycount(count);

                    if(count>0 && _findkey==0){
                        wordCloudMapper.createWordCloudKeywordData(reKeywords); // 키워드 집계
                    }
                }
            }
        }

        for (String[] adjectives : s_adjectives) { //형용사 집계
            if(adjectives!=null && adjectives.length>0){
                for (String adj : adjectives) {
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
                    int _findkey = wordCloudMapper.findWordCloudKeyword(reKeywords);
                    reKeywords.setKeycount(count);

                    if(count>0 && _findkey==0){
                        wordCloudMapper.createWordCloudKeywordData(reKeywords); // 키워드 집계
                    }
                }
            }
        }

    }


}
