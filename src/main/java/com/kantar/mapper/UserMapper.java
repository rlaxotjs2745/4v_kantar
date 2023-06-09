package com.kantar.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.kantar.vo.UserVO;

import java.util.List;

@Mapper
public interface UserMapper {
    public UserVO getUserInfo(UserVO paramVo) throws Exception;

    public Integer savUserInfo(UserVO paramVo) throws Exception;

    public Integer modUserInfo(UserVO paramVo) throws Exception;

    public Integer delUserInfo(UserVO paramVo) throws Exception;

    public List<UserVO> getUserList(UserVO paramVo) throws Exception;

    public UserVO getUserInfoByFCode(UserVO paramVo) throws Exception;

    Integer updateUserPW(UserVO userInfo) throws Exception;

    public List<UserVO>  getApiUserList(UserVO userInfo) throws Exception;

    public Integer savLoginHistory(UserVO paramVo) throws Exception;
}
