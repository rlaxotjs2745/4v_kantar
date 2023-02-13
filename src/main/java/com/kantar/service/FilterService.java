package com.kantar.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.kantar.vo.ReportFilterDataVO;
import com.kantar.vo.ReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kantar.mapper.FilterMapper;
import com.kantar.vo.FilterDataVO;
import com.kantar.vo.FilterVO;

@Service
public class FilterService {
    @Autowired
    private FilterMapper filterMapper;

    public Integer create_Filter(FilterVO paramVo, String _data) throws Exception {
        String[] _data1 = _data.split("//");
        Map<String, Object> _sptd = new HashMap<String, Object>();
        ArrayList<Object> _sptd0 = new ArrayList<Object>();
        for(String _data0 : _data1){
            FilterDataVO param = new FilterDataVO();
            param.setIdx_filter(paramVo.getIdx_filter());
            param.setFilter_type(paramVo.getFilter_type());
            param.setFilter_data(_data0);
            _sptd0.add(param);
        }
        _sptd.put("list",_sptd0);
        Integer rs1 = filterMapper.createFilterData(_sptd);
        if(rs1 == 0){
            filterMapper.delFilter(paramVo);
            return 0;
        }else{
            return 1;
        }
    }

    /**
     * 조건지정 리포트용 필터 생성
     */
    public Integer createReportFilter(ReportVO reportVO) throws Exception {
        Integer rs0 = 0;

        FilterVO paramVo = new FilterVO();
        paramVo.setFilter_title(reportVO.getTitle() + " filter");
        paramVo.setIdx_user(reportVO.getIdx_user());
        paramVo.setIdx_project_job_projectid(reportVO.getIdx_project_job_projectid());

        rs0 = filterMapper.createFilter(paramVo);

        if(rs0 == 1){
            if(StringUtils.isNotEmpty(reportVO.getTp1())){
                paramVo.setFilter_type(1);
                Integer rs1 = create_Filter(paramVo, reportVO.getTp1());
                if(rs1 == 0){
                    filterMapper.delFilter(paramVo);
                }
            }
            if(StringUtils.isNotEmpty(reportVO.getTp2())){
                paramVo.setFilter_type(2);
                Integer rs1 = create_Filter(paramVo, reportVO.getTp2());
                if(rs1 == 0){
                    filterMapper.delFilter(paramVo);
                }
            }
            if(StringUtils.isNotEmpty(reportVO.getTp3())){
                paramVo.setFilter_type(3);
                Integer rs1 = create_Filter(paramVo, reportVO.getTp3());
                if(rs1 == 0){
                    filterMapper.delFilter(paramVo);
                }
            }
            if(StringUtils.isNotEmpty(reportVO.getTp4())){
                paramVo.setFilter_type(4);
                Integer rs1 = create_Filter(paramVo, reportVO.getTp4());
                if(rs1 == 0){
                    filterMapper.delFilter(paramVo);
                }
            }
            if(StringUtils.isNotEmpty(reportVO.getTp5())){
                paramVo.setFilter_type(5);
                Integer rs1 = create_Filter(paramVo, reportVO.getTp5());
                if(rs1 == 0){
                    filterMapper.delFilter(paramVo);
                }
            }
        }
        return paramVo.getIdx_filter();
    }
}
