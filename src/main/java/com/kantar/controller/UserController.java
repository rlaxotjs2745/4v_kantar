package com.kantar.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import jakarta.mail.internet.MimeMessage;

import jakarta.mail.internet.MimeMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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

    private final JavaMailSender mailSender;

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
                    param.put("idx_user",rs.getIdx_user());
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
                return responseService.getSuccessResult("register","이메일 발송에 성공했습니다.");
            }else{
                return responseService.getFailResult("register","회원 가입 후에 이용해주세요.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseService.getFailResult("register","오류가 발생하였습니다.");
    }


    /**
     * 멤버 관리 - 멤버 등록하기
     * @param req
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/create")
    @Transactional
    public CommonResult create(HttpServletRequest req, @RequestBody UserVO paramVo) throws Exception {
        try {
            if(StringUtils.isEmpty(paramVo.getUser_id())){
                return responseService.getFailResult("create","회원 아이디를 입력해주세요.");
            }
            if(StringUtils.isEmpty(paramVo.getUser_name())){
                return responseService.getFailResult("create","이름을 입력해주세요.");
            }
            if(userMapper.getUserInfo(paramVo) != null){
                return responseService.getFailResult("create","이미 존재하는 아이디입니다.");
            }

            Random rd = new Random();//랜덤 객체 생성
            String newPw = "";
            for(int i=0;i<6;i++) {
                newPw = newPw + rd.nextInt(9);
            }

            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String hashedPassword = passwordEncoder.encode(newPw);
            paramVo.setUser_pw(hashedPassword);
            paramVo.setUser_status(0);
            paramVo.setUser_phone("00000000000");
            Integer rs = userMapper.savUserInfo(paramVo);
            paramVo.setUser_pw(newPw);
            if(rs==1){
                String FROM_ADDRESS = "kantar@kantar.co.kr";
                MimeMessage mail = mailSender.createMimeMessage();
                MimeMessageHelper mailHelper = new MimeMessageHelper(mail, true, "UTF-8");

                mailHelper.setFrom(FROM_ADDRESS);
                mailHelper.setTo(paramVo.getUser_id());
                mailHelper.setSubject("[KANTAR] 회원가입 안내");
                mailHelper.setText(paramVo.getUser_pw(), true);

                mailSender.send(mail);
                return responseService.getSuccessResult("create","회원 가입이 완료되었습니다.");
            }else{
                return responseService.getFailResult("create","회원 가입 후에 이용해주세요.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseService.getFailResult("create","오류가 발생하였습니다.");
    }

    /**
     * 회원정보 수정
     * @param req
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/modify")
    public CommonResult modify(HttpServletRequest req, @RequestBody UserVO paramVo) throws Exception {
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo==null){
                return responseService.getFailResult("login","로그인이 필요합니다.");
            }
            paramVo.setIdx_user(uinfo.getIdx_user());
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
    public CommonResult dropout(HttpServletRequest req, @RequestBody UserVO paramVo) throws Exception {
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo==null){
                return responseService.getFailResult("login","로그인이 필요합니다.");
            }
            paramVo.setIdx_user(uinfo.getIdx_user());
//            if(StringUtils.isEmpty(paramVo.getUser_pw())){
//                return responseService.getFailResult("dropout","비밀번호를 입력해주세요.");
//            }
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
    @GetMapping("/list_member")
    public CommonResult getMemberList(HttpServletRequest req, UserVO paramVo) throws Exception {
        try {
            UserVO userInfo = userMapper.getUserInfo(paramVo);
            if(userInfo.getUser_type() == 1){
                return responseService.getFailResult("list_member","관리자만 조회 가능한 기능힙니다.");
            }
            if(paramVo.getRecordCountPerPage() == null || paramVo.getRecordCountPerPage() == 0){
                paramVo.setRecordCountPerPage(10);
            }
            if(paramVo.getCurrentPage() == null || paramVo.getCurrentPage() == 0){
                paramVo.setCurrentPage(0);
            }
            paramVo.setFilter(userInfo.getUser_type());
            paramVo.setFirstIndex(paramVo.getCurrentPage() * paramVo.getRecordCountPerPage());
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
    @GetMapping("/member_detail")
    public CommonResult getMemberDetail(HttpServletRequest req, UserVO paramVo) throws Exception {
        try {
            UserVO userInfo = userMapper.getUserInfo(paramVo);

            UserVO rs = new UserVO();

            rs.setUser_id(userInfo.getUser_id());
            rs.setUser_name(userInfo.getUser_name());
            rs.setUser_phone(userInfo.getUser_phone());
            rs.setIdx_user(userInfo.getIdx_user());
            rs.setUser_type(userInfo.getUser_type());

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


    @PostMapping("/member_info")
    public CommonResult memberInfo(HttpServletRequest req, @RequestBody UserVO paramVo) throws Exception {
        try {
            UserVO userInfo = userMapper.getUserInfo(paramVo);

            if(userInfo!=null){
                UserVO user = new UserVO();
                user.setUser_id(userInfo.getUser_id());
                user.setUser_phone(userInfo.getUser_phone());

                return responseService.getSuccessResult(user, "member_info", "회원 정보를 전달 합니다.");
            } else {
                return responseService.getFailResult("member_info","존재하지 않는 회원입니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseService.getFailResult("member_info","오류가 발생하였습니다.");
    }

    @PostMapping("/member_modify")
    public CommonResult member_modify(HttpServletRequest req, @RequestBody UserVO paramVo) throws Exception {
        try {
            UserVO userInfo = userMapper.getUserInfo(paramVo);

            if(userInfo!=null){
                if(StringUtils.isEmpty(paramVo.getUser_name()) && StringUtils.isEmpty(paramVo.getUser_phone())){
                    return responseService.getFailResult("member_info","필수 정보가 입력되지 않았습니다.");
                }

                if (StringUtils.isNotEmpty(paramVo.getUser_pw())) {
                    if (StringUtils.isEmpty(paramVo.getUser_pw_origin())) {
                        return responseService.getFailResult("member_info","기존 비밀번호를 입력해주세요.");
                    }

                    String regexPw = "(?=.*\\d{1,50})(?=.*[~`!@#$%\\^&*()-+=]{1,50})(?=.*[a-zA-Z]{2,50}).{10,20}$";

                    Matcher matcherPw = Pattern.compile(regexPw).matcher(paramVo.getUser_pw());
                    if (paramVo.getUser_pw().length() < 12) {
                        return responseService.getFailResult("member_info","12자 이상의 비밀번호만 사용할 수 있습니다.");
                    }

                    if (!matcherPw.find()) {
                        return responseService.getFailResult("member_info","영어, 숫자, 특수문자로 조합된 비밀번호만 사용가능합니다.");
                    }

                    BCryptPasswordEncoder passEncoder = new BCryptPasswordEncoder();

                    System.out.println("userInfo.getUser_pw() = " + userInfo.getUser_pw());
                    System.out.println("paramVo.getUser_pw_origin() = " + paramVo.getUser_pw_origin());


                    if (passEncoder.matches(paramVo.getUser_pw_origin(), userInfo.getUser_pw())) {
                        String cg_pw = passEncoder.encode(paramVo.getUser_pw());
                        paramVo.setUser_pw(cg_pw);
                    } else {
                        return responseService.getFailResult("member_info","기존 비밀번호를 다시 확인해 주세요.");
                    }
                }

                Integer rs = userMapper.modUserInfo(paramVo);

                if(rs==1){
                    return responseService.getSuccessResult("member_info","회원 정보를 수정하였습니다.");
                }else{
                    return responseService.getFailResult("member_info","데이터를 다시 확인해주세요");
                }

            } else {
                return responseService.getFailResult("member_info","존재하지 않는 회원입니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseService.getFailResult("member_info","오류가 발생하였습니다.");
    }

}
