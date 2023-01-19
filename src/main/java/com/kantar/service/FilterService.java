package com.kantar.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
            param.setFilter_type(1);
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
}
