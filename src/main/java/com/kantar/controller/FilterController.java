package com.kantar.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kantar.base.BaseController;
import com.kantar.mapper.FilterMapper;
import com.kantar.model.CommonResult;
import com.kantar.service.FilterService;
import com.kantar.service.ResponseService;
import com.kantar.vo.FilterVO;
import com.kantar.vo.UserVO;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/filter")
public class FilterController extends BaseController {
    @Autowired
    private ResponseService responseService;

    @Autowired
    private FilterService filterService;

    @Autowired
    private FilterMapper filterMapper;

    /**
     * 필터 리스트 & 정보
     * 사용방법 : {"idx_proejct_job_projectid":1,"idx_filter":1}
     * @param req
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/get")
    public CommonResult getFilter(HttpServletRequest req, @RequestBody FilterVO paramVo) throws Exception {
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo==null){
                return responseService.getFailResult("login","로그인이 필요합니다.");
            }
            paramVo.setIdx_user(uinfo.getIdx_user());
            if(paramVo.getIdx_project_job_projectid() == null){
                return responseService.getFailResult("filter_create","프로젝트 값이 없습니다.");
            }

            List<FilterVO> rs = filterMapper.getFilter(paramVo);
            if(rs != null){
                return responseService.getSuccessResult(rs, "filter_get", "필터를 불러왔습니다.");
            }else{
                return responseService.getFailResult("filter_get","필터 정보가 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("filter_get","오류가 발생하였습니다.");
        }
    }

    /**
     * 필터 프리셋 삭제
     * @param req
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/del")
    public CommonResult delFilter(HttpServletRequest req, @RequestBody FilterVO paramVo) throws Exception {
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo==null){
                return responseService.getFailResult("login","로그인이 필요합니다.");
            }
            paramVo.setIdx_user(uinfo.getIdx_user());
            if(StringUtils.isEmpty(paramVo.getIdx_filter()+"")){
                return responseService.getFailResult("filter_del","필터 프리셋 값이 없습니다.");
            }

            Integer rs = filterMapper.chkFilterAuth(paramVo);
            if(rs==0){
                return responseService.getFailResult("filter_del","필터 프리셋에 대한 권한이 없습니다.");
            }

            Integer rs1 = filterMapper.chkFilterUse(paramVo);
            if(rs1>1){
                return responseService.getFailResult("filter_del","이용한 필터 프리셋은 삭제할 수 없습니다.");
            }

            Integer rs0 = filterMapper.delFilter(paramVo);
            if(rs0 == 1){
                return responseService.getSuccessResult(rs, "filter_del", "필터 프리셋을 삭제하였습니다.");
            }else{
                return responseService.getFailResult("filter_del","필터 프리셋 정보가 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("filter_del","오류가 발생하였습니다.");
        }
    }

    /**
     * 프로젝트 - 필터 생성
     * <p>filter_tp : 1:화자, 2:챕터, 3:서브챕터, 4:질문</p>
     * <p>filter_data : 화자텍스트//화자텍스트 (구분자 //)</p>
     * <pre>
     * 사용방법 : {"idx_proejct_job_projectid":1,"filter_title":"필터명","tp1":"화자1//화자2","tp2":"챕터1//챕터2","tp3":"서브챕터1//서브챕터2","tp4":"질문1//질문2"}
     * </pre>
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/create")
    @Transactional
    public CommonResult createFilter(HttpServletRequest req, @RequestBody FilterVO paramVo) throws Exception {
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo==null){
                return responseService.getFailResult("login","로그인이 필요합니다.");
            }
            paramVo.setIdx_user(uinfo.getIdx_user());
            if(paramVo.getIdx_project_job_projectid() == null){
                return responseService.getFailResult("filter_create","프로젝트 값이 없습니다.");
            }
            if(paramVo.getFilter_title().equals("")){
                return responseService.getFailResult("filter_create","필터명을 입력해주세요.");
            }

            Integer rs0 = 0;
            if(StringUtils.isNotEmpty(paramVo.getTp3()) && StringUtils.isEmpty(paramVo.getTp2()) ){
                return responseService.getFailResult("filter_create","서브 챕터를 선택했을 때는 챕터 필터 값이 필요합니다.");
            }
            if(StringUtils.isNotEmpty(paramVo.getTp4()) && (StringUtils.isEmpty(paramVo.getTp2()) || StringUtils.isEmpty(paramVo.getTp3())) ){
                return responseService.getFailResult("filter_create","질문을 선택했을 때는 챕터와 서브 챕터 필터 값이 필요합니다.");
            }
            Integer _chk = 0;
            if(StringUtils.isNotEmpty(paramVo.getTp1()) || StringUtils.isNotEmpty(paramVo.getTp2()) || StringUtils.isNotEmpty(paramVo.getTp3()) || StringUtils.isNotEmpty(paramVo.getTp4()) || StringUtils.isNotEmpty(paramVo.getTp5())){
                _chk = 1;
            }
            if(_chk==0){
                return responseService.getFailResult("filter_create","필터를 선택 후 이용해주세요.");
            }
            rs0 = filterMapper.createFilter(paramVo);

            if(rs0 == 1){
                if(StringUtils.isNotEmpty(paramVo.getTp1())){
                    paramVo.setFilter_type(1);
                    Integer rs1 = filterService.create_Filter(paramVo, paramVo.getTp1());
                    if(rs1 == 0){
                        filterMapper.delFilter(paramVo);
                        return responseService.getFailResult("filter_create","화자 필터 데이터를 저장하지 못했습니다.");
                    }
                }
                if(StringUtils.isNotEmpty(paramVo.getTp2())){
                    paramVo.setFilter_type(2);
                    Integer rs1 = filterService.create_Filter(paramVo, paramVo.getTp2());
                    if(rs1 == 0){
                        filterMapper.delFilter(paramVo);
                        return responseService.getFailResult("filter_create","챕터 필터 데이터를 저장하지 못했습니다.");
                    }
                }
                if(StringUtils.isNotEmpty(paramVo.getTp3())){
                    paramVo.setFilter_type(3);
                    Integer rs1 = filterService.create_Filter(paramVo, paramVo.getTp3());
                    if(rs1 == 0){
                        filterMapper.delFilter(paramVo);
                        return responseService.getFailResult("filter_create","서브챕터 필터 데이터를 저장하지 못했습니다.");
                    }
                }
                if(StringUtils.isNotEmpty(paramVo.getTp4())){
                    paramVo.setFilter_type(4);
                    Integer rs1 = filterService.create_Filter(paramVo, paramVo.getTp4());
                    if(rs1 == 0){
                        filterMapper.delFilter(paramVo);
                        return responseService.getFailResult("filter_create","질문 필터 데이터를 저장하지 못했습니다.");
                    }
                }
                if(StringUtils.isNotEmpty(paramVo.getTp5())){
                    paramVo.setFilter_type(5);
                    Integer rs1 = filterService.create_Filter(paramVo, paramVo.getTp5());
                    if(rs1 == 0){
                        filterMapper.delFilter(paramVo);
                        return responseService.getFailResult("filter_create","키워드 필터 데이터를 저장하지 못했습니다.");
                    }
                }
                return responseService.getSuccessResult("filter_create", "필터를 저장하였습니다.");
            }else{
                return responseService.getFailResult("filter_create","필터를 저장하지 못했습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("filter_create","오류가 발생하였습니다.");
        }
    }
}
