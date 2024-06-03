package com.newtouch.controller;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.newtouch.dto.TraderResponse;
import com.newtouch.dto.response.CaptchaResponse;
import com.newtouch.enums.CacheEnum;
import com.newtouch.utils.Captcha;
import com.newtouch.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private RedisUtil redisUtil;

    @GetMapping("/captcha")
    public TraderResponse<CaptchaResponse> captcha() throws Exception {
        Captcha captcha = new Captcha(120, 40, 4, 10);

        redisUtil.addCache(IdWorker.getIdStr(), captcha.getCode(), CacheEnum.CAPTCHA);
        return null;
    }
}
