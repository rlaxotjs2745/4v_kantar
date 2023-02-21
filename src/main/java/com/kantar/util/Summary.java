package com.kantar.util;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kantar.base.BaseController;
import com.kantar.vo.ProjectVO;

@Component
public class Summary {
	@Value("${spring.smr.token}")
    public String smrtoken;

	public ProjectVO getSummary(String pp, String _title) throws Exception {
		String _rs = BaseController.transferHttpPost("https://apis.daglo.ai/nlp/v1/sync/summaries", pp, smrtoken);
		ProjectVO paramVo = new ProjectVO();
		if(!_rs.equals("error")){
			Map<String, String[]> _rss = new Gson().fromJson(_rs, new TypeToken<Map<String, String[]>>(){}.getType());
			String[] _rsss = _rss.get("summaries");
			String[] _rskey = _rss.get("keywords");
			String[] _rsadj = _rss.get("keywordsAdjectives");

			paramVo.setTitle(_title);
			paramVo.setSummary0(_rsss[0]);
			paramVo.setSummary_keywords(_rskey);
			paramVo.setSummary_adjectives(_rsadj);
			if(_rskey!=null && _rskey.length>0){
				paramVo.setKeywords(_rskey[0]);
			} else {
				paramVo.setKeywords("");
			}
			if(_rsadj!=null && _rsadj.length>0){
				paramVo.setAdjectives(_rskey[0]);
			} else {
				paramVo.setAdjectives("");
			}
			paramVo.setDagloerr(0);
		}else{
			System.out.println("DAGLO : " + _rs.toString());
			paramVo.setTitle("");
			paramVo.setSummary0("");
			paramVo.setKeywords("");
			paramVo.setAdjectives("");
			paramVo.setDagloerr(1);
		}
		return paramVo;
	}
}