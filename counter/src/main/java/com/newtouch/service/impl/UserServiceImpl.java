package com.newtouch.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.newtouch.dto.request.LoginRequest;
import com.newtouch.entity.User;
import com.newtouch.enums.CacheEnum;
import com.newtouch.enums.ResponseEnum;
import com.newtouch.mappers.UserMapper;
import com.newtouch.service.UserService;
import com.newtouch.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserMapper userMapper;

    @Override
    public User login(LoginRequest loginRequest) {
        User user = new User();

        // 从redis获取验证码
        String captchaKey = redisUtil.getCache(loginRequest.getCaptchaKey(), CacheEnum.CAPTCHA);

        if (StringUtils.endsWithIgnoreCase(captchaKey, loginRequest.getCaptcha())) {
            // 移除redis验证码缓存
            redisUtil.deleteCache(loginRequest.getCaptchaKey(), CacheEnum.CAPTCHA);

            user = userMapper.getUser(loginRequest);
            if (user != null) {
                String loginToken = IdWorker.getIdStr();
                user.setLoginToken(loginToken);
                user.setLoginMsg(ResponseEnum.SUCCESS.getMsg());

                // 登录信息存入redis
                redisUtil.addCache(loginToken, JSON.toJSONString(user), CacheEnum.USER);

            } else {
                user = new User();
                user.setLoginMsg(ResponseEnum.LOGIN_USER_ERROR.getMsg());
            }
        } else {
            user.setLoginMsg(ResponseEnum.LOGIN_CAPTCHA_ERROR.getMsg());
        }

        return user;
    }

    @Override
    public boolean accountExistInCache(String token) {
        return redisUtil.getCache(token, CacheEnum.USER) != null;
    }

    @Override
    public boolean logout(String loginToken) {
        // 登录信息移出redis
        return redisUtil.deleteCache(loginToken, CacheEnum.USER);
    }

    @Override
    public boolean updatePassword(LoginRequest loginRequest) {
        return userMapper.updatePassword(loginRequest);
    }

    @Override
    public BigDecimal getBalance(LoginRequest loginRequest) {
        return userMapper.getBalance(loginRequest.getUid());
    }
}
