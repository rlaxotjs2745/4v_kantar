package com.kantar.controller;

import com.kantar.base.BaseController;
import com.kantar.mapper.*;
import com.kantar.model.CommonResult;
import com.kantar.service.DictionaryService;
import com.kantar.service.ResponseService;
import com.kantar.service.StatisticsService;
import com.kantar.util.Excel;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController extends BaseController {
    @Autowired
    private final ResponseService responseService;

    @Autowired
    private final StatisticsService statisticsService;

    @Autowired
    private final UserMapper userMapper;

    @Autowired
    private final StatisticsMapper statisticsMapper;

    @Value("${file.upload-dir}")
    public String filepath;

    /**
     * 시스템 사용 현황
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/system_statistics")
    public CommonResult getSystemList(HttpServletRequest req, @RequestBody UserVO paramVo) throws Exception {
        try{
            UserVO userInfo = userMapper.getUserInfo(paramVo);
            if(userInfo.getUser_type() == 1){
                return responseService.getFailResult("system_statistics","관리자만 조회 가능한 기능힙니다.");
            }
            StatisticsVO statisticsVO = statisticsService.getCurrentFileStatistic();

            return responseService.getSuccessResult(statisticsVO, "system_statistics", "시스템 사용 현황이 정상적으로 존재합니다");

        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("system_statistics","오류가 발생하였습니다.");
        }
    }

}
