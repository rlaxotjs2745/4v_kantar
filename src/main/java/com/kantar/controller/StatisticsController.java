package com.kantar.controller;

import com.kantar.base.BaseController;
import com.kantar.mapper.*;
import com.kantar.model.CommonResult;
import com.kantar.service.DictionaryService;
import com.kantar.service.ResponseService;
import com.kantar.service.StatisticsService;
import com.kantar.util.Excel;
import com.kantar.util.TokenJWT;
import com.kantar.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.relational.core.sql.In;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController extends BaseController {
    @Autowired
    private ResponseService responseService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StatisticsMapper statisticsMapper;

    @Value("${file.upload-dir}")
    public String filepath;

    @Autowired
    private TokenJWT tokenJWT;

    /**
     * 시스템 사용 현황
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/system_statistics")
    public CommonResult getSystemList(HttpServletRequest req) throws Exception {
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo == null){
                return responseService.getFailResult("login","로그인이 필요합니다.");
            }
            if(uinfo.getRole_type() == 1){
                return responseService.getFailResult("system_statistics","관리자만 조회 가능한 기능힙니다.");
            }

            StatisticsVO statisticsVO = statisticsService.getCurrentFileStatistic();

            return responseService.getSuccessResult(statisticsVO, "system_statistics", "시스템 사용 현황이 정상적으로 존재합니다");

        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("system_statistics","오류가 발생하였습니다.");
        }
    }


    /**
     * API 사용량 회원 목록
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/api_user")
    public CommonResult apiUserList(HttpServletRequest req) throws Exception {
        try{
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo==null){
                return responseService.getFailResult("login","로그인이 필요합니다.");
            }
            if(uinfo.getRole_type() == 1){
                return responseService.getFailResult("api_user","관리자만 조회 가능한 기능힙니다.");
            }

            List<UserVO> userList = userMapper.getApiUserList(uinfo);

            return responseService.getSuccessResult(userList, "api_user", "유저 목록을 전달합니다");

        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("api_user","오류가 발생하였습니다.");
        }
    }

    /**
     * API 사용량 검색정보
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/api_statistics")
    public CommonResult apiStatisticsByUser(HttpServletRequest req, @RequestBody StatisticsVO paramVo) throws Exception {
        try{
            UserVO uinfo = getChkUserLogin(req);

            if(uinfo==null){
                return responseService.getFailResult("login","로그인이 필요합니다.");
            }
            if(uinfo.getRole_type() == 1){
                return responseService.getFailResult("api_statistics","관리자만 조회 가능한 기능힙니다.");
            }

            Map result = new HashMap();
            double total_data = statisticsMapper.getApiDataByUser();
            //double user_data = statisticsMapper.getApiDataByUser(paramVo);
            long total_api = statisticsMapper.getApiStatisticsByUser();
            long user_api = statisticsMapper.getApiStatisticsByUser(paramVo);
            long total_keyword = statisticsMapper.getKeywordStatisticsByUser();
            long user_keyword = statisticsMapper.getKeywordStatisticsByUser(paramVo);

            result.put("total_data", total_data);
            //result.put("user_data", user_data);
            result.put("user_data", 0);
            result.put("total_summary", total_api);
            result.put("user_summary", user_api);
            result.put("total_keyword", total_keyword);
            result.put("user_keyword", user_keyword);

            return responseService.getSuccessResult(result, "api_statistics", "API 사용량을 전달합니다");

        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("api_statistics","오류가 발생하였습니다.");
        }
    }

}
