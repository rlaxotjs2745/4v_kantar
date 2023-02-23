package com.kantar.controller;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.kantar.base.BaseController;
import com.kantar.mapper.UserMapper;
import com.kantar.model.CommonResult;
import com.kantar.service.ResponseService;
import com.kantar.util.MailSender;
import com.kantar.util.TokenJWT;
import com.kantar.vo.UserVO;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
public class UserController extends BaseController {
    @Autowired
    private ResponseService responseService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TokenJWT tokenJWT;

    @Autowired
    private MailSender mailSender;

    @Value("${w3domain}")
    private String clientDomain;

    /**
     * 로그인 체크
     * @param req
     * @return CommonResult
     * @throws Exception
     */
    @GetMapping("/loginchk")
    public CommonResult loginchk(HttpServletRequest req) throws Exception{
        UserVO uinfo = getChkUserLogin(req);
        if(uinfo==null){
            return responseService.getFailResult("loginchk","로그아웃");
        }else{
            return responseService.getSuccessResult("loginchk", "로그인");
        }
    }

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
                    if(rs.getUser_status() == 98){
                        return responseService.getFailResult("login","정지된 회원입니다.");
                    }
                    if(rs.getUser_status() == 99){
                        return responseService.getFailResult("login","탈퇴한 회원입니다.");
                    }
                    if(rs.getUser_status() == 0){
                        return responseService.getFailResult("login","회원 인증 후 이용해주세요.");
                    }
                    Map<String, Object> param = new HashMap<String, Object>();
                    param.put("idx_user",rs.getIdx_user());
                    param.put("user_id",rs.getUser_id());
                    param.put("user_status", rs.getUser_status());
                    
                    // 로그인 히스토리 추가
                    paramVo.setIdx_user(rs.getIdx_user());
                    paramVo.setRemoteip(getClientIP(req));
                    userMapper.savLoginHistory(paramVo);
                    String _token = tokenJWT.createToken(param, rs.getUser_type()+"");
                    Map<String, Object> param0 = new HashMap<String, Object>();
                    param0.put("token",_token);
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
    // @Transactional
    @PostMapping("/create")
    public CommonResult create(HttpServletRequest req, @RequestBody UserVO paramVo) throws Exception {
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo == null){
                return responseService.getFailResult("create","로그인이 필요합니다.");
            }
            if(uinfo.getRole_type() == 1){
                return responseService.getFailResult("create","생성 권한이 없습니다.");
            }
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

            if(StringUtils.isEmpty(paramVo.getUser_type()+"")){
                paramVo.setUser_type(1);
            }

            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String hashedPassword = passwordEncoder.encode(new Date().toString());
            paramVo.setUser_pw(hashedPassword);
            paramVo.setUser_status(0);
            // paramVo.setUser_phone(newPw);
            paramVo.setFirst_code(newPw);
            Integer rs = userMapper.savUserInfo(paramVo);
            if(rs==1){
                mailSender.sender(paramVo.getUser_id(), "[KANTAR] 회원가입 안내", "<a href=\"" + clientDomain + "/firstlogin/" + newPw + "\">계정 인증하기</a>");
                return responseService.getSuccessResult("create","회원 가입이 완료되었습니다.");
            } else {
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
            UserVO thisUserInfo = getChkUserLogin(req);
            if(thisUserInfo==null){
                return responseService.getFailResult("modify","로그인이 필요합니다.");
            }
            if(thisUserInfo.getRole_type() == 1){
                return responseService.getFailResult("modify","삭제 권한이 없습니다.");
            }
            UserVO uinfo = userMapper.getUserInfo(paramVo);
            if(uinfo == null){
                return responseService.getFailResult("modify","회원이 없습니다.");
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
//            paramVo.setIdx_user(paramVo.getIdx_user());
            if(uinfo.getRole_type() == 1){
                return responseService.getFailResult("dropout","삭제 권한이 없습니다.");
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
    @GetMapping("/list_member")
    public CommonResult getMemberList(HttpServletRequest req, UserVO paramVo) throws Exception {
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo == null){
                return responseService.getFailResult("member_detail","로그인이 필요합니다.");
            }
            paramVo.setIdx_user(uinfo.getIdx_user());
            if(uinfo.getRole_type() == 1){
                return responseService.getFailResult("list_member","관리자만 조회 가능한 기능힙니다.");
            }
            if(paramVo.getRecordCountPerPage() == null || paramVo.getRecordCountPerPage() == 0){
                paramVo.setRecordCountPerPage(10);
            }
            if(paramVo.getCurrentPage() == null || paramVo.getCurrentPage() == 0){
                paramVo.setCurrentPage(0);
            }
            paramVo.setFilter(uinfo.getRole_type());
            paramVo.setFirstIndex(paramVo.getCurrentPage() * paramVo.getRecordCountPerPage());
            Map<String, Object> rs = new HashMap<>();
            List<UserVO> userList = userMapper.getUserList(paramVo);
            if(userList != null){
                rs.put("userList", userList);
                rs.put("idx_user", uinfo.getIdx_user());
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
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo == null){
                return responseService.getFailResult("member_detail","로그인이 필요합니다.");
            }
            if(uinfo.getRole_type() == 1 && uinfo.getIdx_user() != paramVo.getIdx_user()){
                return responseService.getFailResult("member_detail","관리자, 본인만 조회 가능한 기능힙니다.");
            }
            UserVO userInfo = userMapper.getUserInfo(paramVo);
            if(userInfo != null){
                UserVO rs = new UserVO();

                rs.setUser_id(userInfo.getUser_id());
                rs.setUser_name(userInfo.getUser_name());
                rs.setUser_phone(userInfo.getUser_phone());
                rs.setIdx_user(userInfo.getIdx_user());
                rs.setUser_type(userInfo.getUser_type());
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
     * 프로필 정보 불러오기
     * @param req
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/member_info")
    public CommonResult memberInfo(HttpServletRequest req, @RequestBody UserVO paramVo) throws Exception {
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo==null){
                return responseService.getFailResult("login","로그인이 필요합니다.");
            }

            UserVO temp = userMapper.getUserInfo(uinfo);

            UserVO user = new UserVO();
            user.setIdx_user(temp.getIdx_user());
            user.setUser_type(temp.getUser_type());
            user.setUser_id(temp.getUser_id());
            user.setUser_name(temp.getUser_name());
            user.setUser_phone(temp.getUser_phone());

            return responseService.getSuccessResult(user, "member_info", "회원 정보를 전달 합니다.");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseService.getFailResult("member_info","오류가 발생하였습니다.");
    }

    /**
     * 회원 정보 수정 (비밀번호 수정 포함)
     * @param req
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/member_modify")
    public CommonResult memberModify(HttpServletRequest req, @RequestBody UserVO paramVo) throws Exception {
        try {
            UserVO userInfo = userMapper.getUserInfo(paramVo);

            if(userInfo!=null){
                if(StringUtils.isEmpty(paramVo.getUser_name()) && StringUtils.isEmpty(paramVo.getUser_phone())){
                    return responseService.getFailResult("member_modify","필수 정보가 입력되지 않았습니다.");
                }

                if (StringUtils.isNotEmpty(paramVo.getUser_pw())) {
                    if (StringUtils.isEmpty(paramVo.getUser_pw_origin())) {
                        return responseService.getFailResult("member_modify","기존 비밀번호를 입력해주세요.");
                    }

                    BCryptPasswordEncoder passEncoder = new BCryptPasswordEncoder();

                    if (passEncoder.matches(paramVo.getUser_pw_origin(), userInfo.getUser_pw())) {

                        String regexPw = "(?=.*\\d{1,50})(?=.*[~`!@#$%\\^&*()-+=]{1,50})(?=.*[a-zA-Z]{2,50}).{10,20}$";
                        Matcher matcherPw = Pattern.compile(regexPw).matcher(paramVo.getUser_pw());

                        if (paramVo.getUser_pw().length() > 12 || paramVo.getUser_pw().length() < 8) {
                            return responseService.getFailResult("member_modify","비밀번호는 8~12자 사이로 지정해주세요.");
                        }

                        if (!matcherPw.find()) {
                            return responseService.getFailResult("member_modify","영어, 숫자, 특수문자로 조합된 비밀번호만 사용가능합니다.");
                        }
                        String cg_pw = passEncoder.encode(paramVo.getUser_pw());
                        paramVo.setUser_pw(cg_pw);
                    } else {
                        return responseService.getFailResult("member_modify","기존 비밀번호를 다시 확인해 주세요.");
                    }
                } else {
                    paramVo.setUser_pw(userInfo.getUser_pw());
                }
                Integer rs = userMapper.modUserInfo(paramVo);

                if(rs==1){
                    return responseService.getSuccessResult("member_modify","회원 정보를 수정하였습니다.");
                }else{
                    return responseService.getFailResult("member_modify","데이터를 다시 확인해주세요");
                }

            } else {
                return responseService.getFailResult("member_modify","존재하지 않는 회원입니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseService.getFailResult("member_modify","오류가 발생하였습니다.");
    }

    /**
     * 새 비밀번호 발송
     * @param req
     * @param paramVo
     * @return
     * @throws Exception
     */
    @PostMapping("/find_pw")
    @Transactional
    public CommonResult findPw(HttpServletRequest req, @RequestBody UserVO paramVo) throws Exception {
        try{
            UserVO userInfo = userMapper.getUserInfo(paramVo);

            if(userInfo!=null){

                String tempPW = mailSender.getRamdomPassword();
                BCryptPasswordEncoder passEncoder = new BCryptPasswordEncoder();
                String cgPw = passEncoder.encode(tempPW);
                userInfo.setUser_pw(cgPw);

                Integer rs = userMapper.updateUserPW(userInfo);

                if(rs==1){
                    mailSender.sender(userInfo.getUser_id(), "[KANTAR] 임시비밀번호 발급", tempPW);
                    return responseService.getSuccessResult("findPw","임시 비밀번호가 발급되었습니다.");
                }else{
                    return responseService.getFailResult("findPw","메일 발송을 실패하였습니다.");
                }

            } else {
                return responseService.getFailResult("findPw","존재하지 않는 회원입니다.");
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        return responseService.getFailResult("findPw","오류가 발생하였습니다.");
    }

    @GetMapping("/first_login")
    public CommonResult firstLogin(HttpServletRequest req, @RequestParam("fCode") String firstCode) throws Exception {
        try {
            UserVO userInfo = userMapper.getUserInfoByFCode(firstCode);
            if(userInfo == null){
                return responseService.getFailResult("first_login","사용자를 찾을 수 없습니다.");
            }
            userInfo.setUser_pw(null);
            userInfo.setFirst_code(null);
            return responseService.getSuccessResult(userInfo, "first_login", "멤버 불러오기 성공");
        } catch (Exception e){
            e.printStackTrace();
            return responseService.getFailResult("first_login","오류가 발생했습니다.");
        }
    }

    /**
     * 첫 인증 및 휴대폰,비밀번호 설정
     * @param req
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/first_login_confirm")
    public CommonResult first_login_confirm(HttpServletRequest req, @RequestBody UserVO paramVo) throws Exception {
        try {
            UserVO uinfo = userMapper.getUserInfo(paramVo);
            if(uinfo == null){
                return responseService.getFailResult("first_login_confirm","회원이 없습니다.");
            }
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String hashedPassword = passwordEncoder.encode(paramVo.getUser_pw());
            paramVo.setUser_pw(hashedPassword);
            paramVo.setUser_status(1);
            Integer rs = userMapper.modUserInfo(paramVo);
            if(rs==1){
                return responseService.getSuccessResult("first_login_confirm","회원 인증이 완료되었습니다. 로그인 후 이용해주세요.");
            }else{
                return responseService.getFailResult("first_login_confirm","회원 가입 후에 이용해주세요.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseService.getFailResult("first_login_confirm","오류가 발생하였습니다.");
    }

    /**
     * 헤더 권한
     * @param req
     * @return CommonResult
     * @throws Exception
     */
    @GetMapping("/header_info")
    public CommonResult getHeaderInfo(HttpServletRequest req) throws Exception {
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo == null){
                return responseService.getFailResult("header_info","로그인이 필요합니다.");
            }
            Map<String, Object> rs = new HashMap<>();
            rs.put("user_type", uinfo.getRole_type());
            return responseService.getSuccessResult(rs, "header_info", "회원 타입 불러오기 성공");
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("header_info","오류가 발생하였습니다.");
        }

    }

}
