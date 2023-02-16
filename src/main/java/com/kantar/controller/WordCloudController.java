package com.kantar.controller;

import com.kantar.base.BaseController;
import com.kantar.mapper.FilterMapper;
import com.kantar.mapper.ProjectMapper;
import com.kantar.mapper.ReportMapper;
import com.kantar.mapper.WordCloudMapper;
import com.kantar.model.CommonResult;
import com.kantar.service.*;
import com.kantar.util.TokenJWT;
import com.kantar.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/word")
public class WordCloudController extends BaseController {
    @Autowired
    private ResponseService responseService;

    @Autowired
    private WordCloudService wordCloudService;

    @Autowired
    private WordCloudMapper wordCloudMapper;

    @Autowired
    private TokenJWT tokenJWT;

    @Value("${file.upload-dir}")
    public String filepath;


    /**
     * 워드 클라우드 생성하기
     * @param req
     * @param wordCloudVO
     * @return
     * @throws Exception
     */
    @PostMapping("/save_word_cloud")
    @Transactional
    public CommonResult save_wordCloud(HttpServletRequest req, @RequestBody WordCloudVO wordCloudVO) throws Exception {
        String _token = tokenJWT.resolveToken(req);
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo==null){
                return responseService.getFailResult("save_word_cloud","로그인이 필요합니다.");
            }

            wordCloudVO.setIdx_user(uinfo.getIdx_user());

            if(StringUtils.isEmpty(wordCloudVO.getTitle())){
                return responseService.getFailResult("save_word_cloud","워드 클라우드의 이름을 입력해주세요.");
            }

            if(wordCloudVO.getIdx_project_job_projectid()==null){
                return responseService.getFailResult("save_word_cloud","프로젝트 정보를 다시 확인해주세요");
            }

            if(wordCloudVO.getKeyType()==null){
                wordCloudVO.setKeyType(3); // 1:명사만 추출 / 2:형용사만 추출 / 3:둘다 추출(디폴트)
            }

            Integer idx_filter = wordCloudService.createWordCloudFilter(wordCloudVO);
            wordCloudVO.setIdx_word_filter(idx_filter);
            wordCloudService.createWordCloud(_token, wordCloudVO);

            return responseService.getSuccessResult("save_word_cloud", "워드 클라우드 생성을 시작하였습니다.");

        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("save_word_cloud","오류가 발생하였습니다.");
        }
    }


    /**
     * 워드클라우드 리스팅
     * @param req
     * @param wc
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/list_wordcloud")
    public CommonResult getProjectList(HttpServletRequest req, @RequestBody WordCloudVO wc) throws Exception {
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo==null){
                return responseService.getFailResult("login","로그인이 필요합니다.");
            }
            wc.setIdx_user(uinfo.getIdx_user());

            if(wc.getCurrentPage() == null){
                wc.setCurrentPage(1);
            }
            wc.setRecordCountPerPage(10);
            wc.setFirstIndex((wc.getCurrentPage()-1) * 10);
            Integer tcnt = wordCloudMapper.getWordCloudListCount(wc);
            List<FilterVO> rs = wordCloudMapper.getWordCloudList(wc);

            Map<String, Object> _data = new HashMap<String, Object>();
            _data.put("tcnt",tcnt);
            _data.put("list",rs);

            if(rs!=null){
                return responseService.getSuccessResult(_data, "list_wordcloud", "워드클라우드 리스팅 성공");
            }else{
                return responseService.getFailResult("list_wordcloud","워드클라우드 리스트가 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("list_wordcloud","오류가 발생하였습니다.");
        }
    }


    /**
     * 워드클라우드 상세보기
     * @param req
     * @param wc
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/wordcloud_view")
    public CommonResult wordcloud_view(HttpServletRequest req, @RequestBody WordCloudVO wc) throws Exception {
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo==null){
                return responseService.getFailResult("login","로그인이 필요합니다.");
            }
            wc.setIdx_user(uinfo.getIdx_user());

            List<WordCloudDataVO> keyword = wordCloudMapper.getWordCloudDetail(wc);
            List<FilterVO> filter = wordCloudMapper.getWordCloudFilterDetail(wc);

            Map<String, Object> _data = new HashMap<String, Object>();
            _data.put("keyword",keyword);
            _data.put("filter",filter);

            if(!keyword.isEmpty()){
                return responseService.getSuccessResult(_data, "wordcloud_view", "워드클라우드 정보를 불러왔습니다");
            }else{
                return responseService.getFailResult("wordcloud_view","일치하는 워드클라우드 리스트가 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("wordcloud_view","오류가 발생하였습니다.");
        }
    }

}
