package com.newtouch.service;

import com.newtouch.dto.request.LoginRequest;
import com.newtouch.entity.User;

import java.math.BigDecimal;

public interface UserService {
    /**
     * 登陆
     *
     * @param loginRequest
     * @return User
     * @throws Exception
     */
    User login(LoginRequest loginRequest);

    /**
     * 缓存中是否存在已登录信息
     *
     * @param token
     * @return
     */
    boolean accountExistInCache(String token);


    /**
     * 退出登录
     *
     * @param loginToken
     * @return
     */
    boolean logout(String loginToken);


    /**
     * 更新密码
     * @param loginRequest
     * @return
     */
    boolean updatePassword(LoginRequest loginRequest);

    BigDecimal getBalance(LoginRequest loginRequest);
}
