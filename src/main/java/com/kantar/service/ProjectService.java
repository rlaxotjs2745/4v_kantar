package com.kantar.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

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
import com.kantar.vo.ProjectVO;
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
    public void create_report(List<ProjectVO> prs, ProjectVO paramVo) throws Exception{
        try {
            System.out.println("create_report START");

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
                for(String[] _ers0 : ers){
                    if(j>0){
                        int i = 0;
                        SumtextVO _elist = new SumtextVO();
                        for(String _ers00 : _ers0){
                            if(i==3){
                                _elist.setSpeaker(_ers00.toString());
                            }
                            if(i==2){
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
                // _nlp.put("keywordExtraction",_nlp0);
                // _nlp.put("sentimentAnalysis",_nlp0);
                params.setNlpConfig(_nlp);
                String pp = new Gson().toJson(params);
                System.out.println("JSON : " + pp);
                String _rs = BaseController.transferHttpPost("https://apis.daglo.ai/nlp/v1/sync/summaries", pp, smrtoken);
                Map<String, String[]> _rss = new Gson().fromJson(_rs, new TypeToken<Map<String, String[]>>(){}.getType());
                String[] _rsss = _rss.get("summaries");
                int ii = 0;
                for(String rss : _rsss){
                    paramVo.setTitle("");
                    paramVo.setSummary0(rss);
                    ProjectVO ridx = projectMapper.getReportIdx(paramVo);
                    Integer ridx0 = 0;
                    if(ridx==null){
                        ridx0 = projectMapper.savReportIdx(paramVo);
                    }else{
                        paramVo.setIdx_report(ridx.getIdx_report());
                        ridx0 = 1;
                    }
                    if(ridx0==1){
                        projectMapper.saveReportData(paramVo);
                    }
                    ii++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
