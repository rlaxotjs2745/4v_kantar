package com.kantar.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kantar.base.BaseController;
import com.kantar.mapper.UserMapper;
import com.kantar.model.CommonResult;
import com.kantar.service.ResponseService;
import com.kantar.vo.UserVO;

@RestController("/api")
public class UserController extends BaseController {
    private ResponseService responseService;
    private UserMapper userMapper;

    /**
     * 로그인
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/login")
    public CommonResult login(UserVO paramVo) throws Exception {
        try {
            UserVO rs = userMapper.getUserInfo(paramVo);
            return responseService.getSuccessResult(rs);
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("999","오류가 발생하였습니다.");
        }
    }
}
