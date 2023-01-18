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

	public ProjectVO getSummary(String pp) throws Exception {
		String _rs = BaseController.transferHttpPost("https://apis.daglo.ai/nlp/v1/sync/summaries", pp, smrtoken);
		ProjectVO paramVo = new ProjectVO();
		if(!_rs.equals("error")){
			Map<String, String[]> _rss = new Gson().fromJson(_rs, new TypeToken<Map<String, String[]>>(){}.getType());
			String[] _rsss = _rss.get("summaries");
			paramVo.setTitle("전체 요약문");
			paramVo.setSummary0(_rsss[0]);
		}else{
			paramVo.setTitle("");
			paramVo.setSummary0("");
		}
		return paramVo;
	}
}