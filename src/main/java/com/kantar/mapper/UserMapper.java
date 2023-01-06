package com.kantar.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.kantar.vo.UserVO;

@Mapper
public interface UserMapper {
    public UserVO getUserInfo(UserVO paramVo) throws Exception;

    public Integer savUserInfo(UserVO paramVo) throws Exception;

    public Integer modUserInfo(UserVO paramVo) throws Exception;

    public Integer delUserInfo(UserVO paramVo) throws Exception;
}
