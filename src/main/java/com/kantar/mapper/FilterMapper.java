package com.kantar.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.kantar.vo.FilterDataVO;
import com.kantar.vo.FilterVO;

@Mapper
public interface FilterMapper {
    public List<FilterVO> getFilter(FilterVO paramVo) throws Exception;

    public Integer chkFilterAuth(FilterVO paramVo) throws Exception;

    public Integer createFilter(FilterVO paramVo) throws Exception;

    public Integer delFilter(FilterVO paramVo) throws Exception;

    public Integer createFilterData(Map<String, Object> paramVo) throws Exception;

    public Integer delFilterData(FilterVO paramVo) throws Exception;

    public Integer chkFilterUse(FilterVO paramVo) throws Exception;

    public List<FilterDataVO> getReportFilterByIdx(Integer idx) throws Exception;

    public List<FilterDataVO> getReportFilter(Integer idx) throws Exception;
}
