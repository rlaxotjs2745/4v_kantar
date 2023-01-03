package com.kantar.interceptor;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.kantar.util.TokenJWT;
import com.kantar.vo.UserVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
@Component
public class ApiAuthorityInterceptor implements HandlerInterceptor {

	@Autowired
	private TokenJWT tokenJWT;

	@Override
	public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        Boolean _r = false;
        try {
			Integer lvl = 0;
			String token = tokenJWT.resolveToken(req);
			if(!"".equals(token) && token != null && !token.isEmpty()){
				Map<String, Object> jwt = tokenJWT.verifyJWT(token);
				if (jwt != null) {
					UserVO a = tokenJWT.getRoles(token);
					lvl = a.getUser_type();
				}
			}

			if(lvl>0){
				_r = true;
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
		if(_r==false){
			Map<String, Object> _resultMap = new HashMap<String, Object>();
			_resultMap.put("success", "0");
			_resultMap.put("code", "-1003");
			_resultMap.put("data", "A resource that can not be accessed with the privileges it has.");
			res.getWriter().write(new Gson().toJson(_resultMap));
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");
			res.setStatus(400);
		}
		return _r;
	}
}
