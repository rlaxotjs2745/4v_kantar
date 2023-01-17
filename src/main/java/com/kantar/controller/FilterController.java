package com.kantar.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.kantar.service.ResponseService;
import com.kantar.vo.FilterDataVO;
import com.kantar.vo.FilterVO;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/filter")
public class FilterController extends BaseController {
    @Autowired
    private ResponseService responseService;

    @Autowired
    private FilterMapper filterMapper;

    @PostMapping("/get")
    @Transactional
    public CommonResult getFilter(HttpServletRequest req, @RequestBody FilterVO paramVo) throws Exception {
        try {
            if(paramVo.getIdx_project() == null){
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
     * 프로젝트 - 필터 생성
     * <p>filter_tp : 1:화자, 2:챕터, 3:서브챕터, 4:질문</p>
     * <p>filter_data : 화자텍스트//화자텍스트 (구분자 //)</p>
     * <pre>
     * 사용방법 : {"idx_project":1,"filter_title":"필터명","filter_tp":"1", "filter_data":"화자1//화자2"}
     * </pre>
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/create")
    @Transactional
    public CommonResult createFilter(HttpServletRequest req, @RequestBody FilterVO paramVo) throws Exception {
        try {
            if(paramVo.getIdx_project() == null){
                return responseService.getFailResult("filter_create","프로젝트 값이 없습니다.");
            }
            if(paramVo.getFilter_title().equals("")){
                return responseService.getFailResult("filter_create","필터명을 입력해주세요.");
            }
            if(StringUtils.isEmpty(paramVo.getFilter_tp())){
                return responseService.getFailResult("filter_create","필터 형식이 없습니다.");
            }else{
                paramVo.setFilter_type(Integer.valueOf(paramVo.getFilter_tp()));
            }
            if(StringUtils.isEmpty(paramVo.getFilter_data())){
                return responseService.getFailResult("filter_create","필터 데이터가 없습니다.");
            }
            String[] _data = paramVo.getFilter_data().split("//");
            if(_data.length > 0){
                Integer rs0 = filterMapper.createFilter(paramVo);
                if(rs0 == 1){
                    Map<String, Object> _sptd = new HashMap<String, Object>();
                    ArrayList<Object> _sptd0 = new ArrayList<Object>();
                    for(String _data0 : _data){
                        FilterDataVO param = new FilterDataVO();
                        param.setIdx_filter(paramVo.getIdx_filter());
                        param.setFilter_type(paramVo.getFilter_type());
                        param.setFilter_data(_data0);
                        _sptd0.add(param);
                    }
                    _sptd.put("list",_sptd0);
                    Integer rs1 = filterMapper.createFilterData(_sptd);
                    if(rs1 > 0){
                        return responseService.getSuccessResult("filter_create", "필터를 저장하였습니다.");
                    }else{
                        filterMapper.delFilter(paramVo);
                        return responseService.getFailResult("filter_create","필터 데이터를 저장하지 못했습니다.");
                    }
                }else{
                    return responseService.getFailResult("filter_create","필터를 저장하지 못했습니다.");
                }
            }else{
                return responseService.getFailResult("filter_create","필터 데이터가 없어서 저장을 중단합니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("filter_create","오류가 발생하였습니다.");
        }
    }
}
