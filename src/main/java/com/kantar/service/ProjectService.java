package com.kantar.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kantar.base.BaseController;
import com.kantar.mapper.ProjectMapper;
import com.kantar.util.Excel;
import com.kantar.util.TokenJWT;
import com.kantar.vo.ProjectVO;
import com.kantar.vo.ProjectViewVO;
import com.kantar.vo.SummaryVO;
import com.kantar.vo.SumtextVO;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ProjectService {
    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private Excel excel;

    @Autowired
    private KafkaSender kafkaSender;

    @Autowired
    private TokenJWT tokenJWT;
    
    @Value("${file.upload-dir}")
    public String filepath;

    @Value("${spring.smr.token}")
    public String smrtoken;

    /**
     * 비동기 리포트 결과 만들기
     * @param prs
     * @param paramVo
     * @throws Exception
     */
    @Async
    @Transactional
    public void create_report(HttpServletRequest req, ProjectVO paramVo) throws Exception{
        try {
            String _token = tokenJWT.resolveToken(req);
            // System.out.println("create_report START");
            List<ProjectVO> prs = projectMapper.getReportFileList(paramVo);

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
                _nlp1.put("maxCount","small");

                _nlp2.put("enable",true);

                _nlp.put("summary",_nlp0);
                // _nlp.put("keywordExtraction",_nlp1);
                // _nlp.put("sentimentAnalysis",_nlp2);
                params.setNlpConfig(_nlp);
                String pp = new Gson().toJson(params);
                // System.out.println("JSON : " + pp);
                String _rs = BaseController.transferHttpPost("https://apis.daglo.ai/nlp/v1/sync/summaries", pp, smrtoken);
                // System.out.println(_rs);
                if(!_rs.equals("error")){
                    Map<String, String[]> _rss = new Gson().fromJson(_rs, new TypeToken<Map<String, String[]>>(){}.getType());
                    String[] _rsss = _rss.get("summaries");
                    int ii = 0;
                    for(String rss : _rsss){
                        paramVo.setTitle("");
                        paramVo.setSummary0(rss);
                        ProjectVO ridx = projectMapper.getReportIdx(paramVo);
                        Integer ridx0 = 0;
                        if(ridx==null){
                            Integer _seq = projectMapper.getReportSeq();
                            _seq = _seq+1;
                            String b1 = ("000"+_seq);
                            String RPID = "R" + b1.substring(b1.length()-4,b1.length());
                            paramVo.setReport_seq(_seq);
                            paramVo.setReport_id(RPID);
                            paramVo.setTitle(paramVo.getProject_name() + "_기본리포트");
                            ridx0 = projectMapper.savReport(paramVo);
                        }else{
                            paramVo.setIdx_report(ridx.getIdx_report());
                            ridx0 = 1;
                        }
                        if(ridx0==1){
                            projectMapper.saveReportData(paramVo);
                        }
                        ii++;
                    }
                    if(StringUtils.isNotEmpty(_token)){
                        kafkaSender.send(tokenJWT.resolveToken(req), "리포트가 생성되었습니다.");
                    }
                }else{
                    if(StringUtils.isNotEmpty(_token)){
                        kafkaSender.send(tokenJWT.resolveToken(req), "리포트 생성을 실패하였습니다.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 프로젝트 상세보기 리스트 만들기
     * @param rlist
     * @param prs0
     * @return ArrayList<ProjectViewVO>
     * @throws Exception
     */
    public ArrayList<ProjectViewVO> get_projectListView(ArrayList<ProjectViewVO> rlist, ProjectVO prs0) throws Exception {
        String _fpath = this.filepath + prs0.getFilepath() + prs0.getFilename();
        rlist = getCsvParse(rlist, _fpath);
        return rlist;
    }

    public ArrayList<ProjectViewVO> getCsvParse(ArrayList<ProjectViewVO> rlist, String _fPath) throws Exception {
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
     * 리포트 필터 적용해서 만들기
     * @param req
     * @param paramVo
     * @throws Exception
     */
    public void list_reportfilter(HttpServletRequest req, ProjectVO paramVo) throws Exception{
        try {
            projectMapper.getReportFilterList(paramVo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
