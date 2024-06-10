package com.newtouch.controller;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.newtouch.dto.TraderResponse;
import com.newtouch.dto.request.LoginRequest;
import com.newtouch.dto.response.CaptchaResponse;
import com.newtouch.entity.User;
import com.newtouch.enums.CacheEnum;
import com.newtouch.enums.ResponseEnum;
import com.newtouch.service.UserService;
import com.newtouch.utils.CaptchaUtil;
import com.newtouch.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserService userService;

    @GetMapping("/captcha")
    public TraderResponse<CaptchaResponse> captcha() throws Exception {
        // 1.生成验证码 120 40 4个字符 噪点+线条
        CaptchaUtil captchaUtil = new CaptchaUtil(120, 40, 4, 10);

        // 2.将验证码<ID,验证码数值>放入缓存
        String key = IdWorker.getIdStr();
        redisUtil.addCache(key, captchaUtil.getCode(), CacheEnum.CAPTCHA);

        // 3.使用base64编码图片，并返回给前台
        CaptchaResponse captchaResponse = CaptchaResponse.builder()
                .captchaKey(key).imageBase64(captchaUtil.getBase64ByteStr())
                .build();

        TraderResponse<CaptchaResponse> result = new TraderResponse<>();
        return result.success(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), captchaResponse);
    }

    @PostMapping("/login")
    public TraderResponse<User> login(@RequestBody @Validated LoginRequest loginRequest) {
        User user = userService.login(loginRequest);

        TraderResponse<User> result = new TraderResponse<>();
        return user.getLoginToken() != null ? result.success(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), user) :
                result.fail(ResponseEnum.FAIL.getCode(), user.getLoginMsg(), user);
    }

    @GetMapping("/loginFail")
    public TraderResponse<String> loginFail() {
        TraderResponse<String> result = new TraderResponse<>();
        return result.fail(ResponseEnum.LOGIN_REDIRECT_ERROR.getCode(), ResponseEnum.LOGIN_REDIRECT_ERROR.getMsg(), null);
    }

    @PostMapping("/logout")
    public TraderResponse<Boolean> logout(@RequestParam String loginToken) {
        Boolean logout = userService.logout(loginToken);

        TraderResponse<Boolean> result = new TraderResponse<>();
        return result.success(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), logout);
    }

    @PostMapping("/updatePassword")
    public TraderResponse<Boolean> updatePassword(@RequestBody @Validated LoginRequest loginRequest) {
        boolean updatePassword = userService.updatePassword(loginRequest);

        TraderResponse<Boolean> result = new TraderResponse<>();
        return updatePassword ? result.success(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), true) :
                result.fail(ResponseEnum.UPDATE_PASSWORD_ERROR.getCode(), ResponseEnum.UPDATE_PASSWORD_ERROR.getMsg(), false);
    }
}
