package com.kantar.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.kantar.vo.UserVO;

@Mapper
public interface UserMapper {
    public UserVO getUserInfo(UserVO paramVo) throws Exception;
}
