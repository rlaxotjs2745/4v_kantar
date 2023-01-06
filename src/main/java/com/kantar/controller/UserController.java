package com.kantar.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kantar.base.BaseController;
import com.kantar.mapper.UserMapper;
import com.kantar.model.CommonResult;
import com.kantar.service.KafkaSender;
import com.kantar.service.ResponseService;
import com.kantar.util.TokenJWT;
import com.kantar.vo.UserVO;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController extends BaseController {
    private final ResponseService responseService;
    private final UserMapper userMapper;
    private final TokenJWT tokenJWT;
    private final KafkaSender kafkaSender;

    /**
     * 로그인
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/login")
    public CommonResult login(HttpServletRequest req, UserVO paramVo) throws Exception {
        try {
            UserVO rs = userMapper.getUserInfo(paramVo);
            if(rs!=null){
                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                if (passwordEncoder.matches(paramVo.getUser_pw(), rs.getUser_pw())) {
                    Map<String, Object> param = new HashMap<String, Object>();
                    param.put("user_id",rs.getUser_id());
                    param.put("user_status", rs.getUser_status());
                    String _token = tokenJWT.createToken(param, rs.getUser_type()+"");
                    Map<String, Object> param0 = new HashMap<String, Object>();
                    param0.put("token",_token);
                    // kafkaSender.send("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyb2xlX3R5cGUiOiIwIiwidXNlcl9pZCI6InRlc3QiLCJ1c2VyX3N0YXR1cyI6MCwiZXhwIjoxNjcyOTM1NTI3fQ._R0SrutcbfTHumtM6LJ0G3pLcb1jRtMWiqD-Xwn9tWE", "login complete");
                    return responseService.getSuccessResult(param0, "login","로그인 성공");
                }else{
                    return responseService.getFailResult("login","회원 가입 후에 이용해주세요.");
                }
            }else{
                return responseService.getFailResult("login","회원 가입 후에 이용해주세요.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseService.getFailResult("login","오류가 발생하였습니다.");
    }

    @PostMapping("/register")
    public CommonResult register(HttpServletRequest req, UserVO paramVo) throws Exception {
        try {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String hashedPassword = passwordEncoder.encode(paramVo.getUser_pw());
            paramVo.setUser_pw(hashedPassword);
            Integer rs = userMapper.savUserInfo(paramVo);
            if(rs==1){
                return responseService.getSuccessResult();
            }else{
                return responseService.getFailResult("register","회원 가입 후에 이용해주세요.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseService.getFailResult("register","오류가 발생하였습니다.");
    }

    @PostMapping("/modify")
    public CommonResult modify(HttpServletRequest req, UserVO paramVo) throws Exception {
        try {
            Integer rs = userMapper.modUserInfo(paramVo);
            if(rs==1){
                return responseService.getSuccessResult("modify","회원 정보를 수정하였습니다.");
            }else{
                return responseService.getFailResult("modify","회원 가입 후에 이용해주세요.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseService.getFailResult("modify","오류가 발생하였습니다.");
    }

    @PostMapping("/dropout")
    public CommonResult dropout(HttpServletRequest req, UserVO paramVo) throws Exception {
        try {
            Integer rs = userMapper.delUserInfo(paramVo);
            if(rs==1){
                return responseService.getSuccessResult("dropout","회원 탈퇴가 완료되었습니다.");
            }else{
                return responseService.getFailResult("dropout","회원 가입 후에 이용해주세요.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseService.getFailResult("dropout","오류가 발생하였습니다.");
    }
}
