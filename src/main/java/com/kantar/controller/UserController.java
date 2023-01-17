package com.kantar.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kantar.base.BaseController;
import com.kantar.mapper.UserMapper;
import com.kantar.model.CommonResult;
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

    /**
     * 로그인
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/login")
    public CommonResult login(HttpServletRequest req, @RequestBody UserVO paramVo) throws Exception {
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

    /**
     * 회원가입
     * @param req
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/register")
    public CommonResult register(HttpServletRequest req,@RequestBody UserVO paramVo) throws Exception {
        try {
            if(StringUtils.isEmpty(paramVo.getUser_id())){
                return responseService.getFailResult("register","회원 아이디를 입력해주세요.");
            }
            if(StringUtils.isEmpty(paramVo.getUser_pw())){
                return responseService.getFailResult("register","비밀번호를 입력해주세요.");
            }
            if(StringUtils.isEmpty(paramVo.getUser_name())){
                return responseService.getFailResult("register","이름을 입력해주세요.");
            }
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String hashedPassword = passwordEncoder.encode(paramVo.getUser_pw());
            paramVo.setUser_pw(hashedPassword);
            Integer rs = userMapper.savUserInfo(paramVo);
            if(rs==1){
                return responseService.getSuccessResult("register","회원 가입이 완료되었습니다.");
            }else{
                return responseService.getFailResult("register","회원 가입 후에 이용해주세요.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseService.getFailResult("register","오류가 발생하였습니다.");
    }

    /**
     * 회원정보 수정
     * @param req
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
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

    /**
     * 회원탈퇴
     * @param req
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/dropout")
    public CommonResult dropout(HttpServletRequest req, UserVO paramVo) throws Exception {
        try {
            if(StringUtils.isEmpty(paramVo.getUser_pw())){
                return responseService.getFailResult("register","비밀번호를 입력해주세요.");
            }
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

    /**
     * 멤버관리(리스팅)
     * @param req
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/list_member")
    public CommonResult getMemberList(HttpServletRequest req, UserVO paramVo) throws Exception {
        try {
            UserVO userInfo = userMapper.getUserInfo(paramVo);
            if(userInfo.getUser_type() == 1){
                return responseService.getFailResult("list_member","관리자만 조회 가능한 기능힙니다.");
            }
            if(paramVo.getRecordCountPerPage() == 0){
                paramVo.setRecordCountPerPage(10);
            }
            if(paramVo.getCurrentPage() == 0){
                paramVo.setCurrentPage(1);
            }
            paramVo.setFilter(userInfo.getUser_type());
            paramVo.setFirstIndex(paramVo.getCurrentPage() * paramVo.getRecordCountPerPage() - (paramVo.getRecordCountPerPage() - 1));
            List<UserVO> rs = userMapper.getUserList(paramVo);
            if(rs != null){
                return responseService.getSuccessResult(rs, "list_member", "멤버 리스팅 성공");
            } else {
                return responseService.getFailResult("list_member","멤버 리스트가 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("list_member","오류가 발생하였습니다.");
        }

    }

    /**
     * 멤버관리(상세보기)
     * @param req
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/member_detail")
    public CommonResult getMemberDetail(HttpServletRequest req, UserVO paramVo) throws Exception {
        try {
            UserVO rs = userMapper.getUserInfo(paramVo);

            if(rs != null){
                return responseService.getSuccessResult(rs, "member_detail", "회원 불러오기 성공");
            } else {
                return responseService.getFailResult("member_detail","회원이 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("member_detail","오류가 발생하였습니다.");
        }
    }

    /**
     * 멤버관리(회원 삭제)
     * @param req
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/member_delete")
    public CommonResult deleteMember(HttpServletRequest req, UserVO paramVo) throws Exception {
        try {
            Integer rs = userMapper.delUserInfo(paramVo);
            if(rs==1){
                return responseService.getSuccessResult("member_delete","회원 삭제가 완료되었습니다.");
            }else{
                return responseService.getFailResult("member_delete","없는 회원입니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseService.getFailResult("member_delete","오류가 발생하였습니다.");
    }


}
