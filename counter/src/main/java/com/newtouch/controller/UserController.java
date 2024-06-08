package com.newtouch.controller;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.newtouch.dto.TraderResponse;
import com.newtouch.dto.response.CaptchaResponse;
import com.newtouch.enums.CacheEnum;
import com.newtouch.enums.ResponseEnum;
import com.newtouch.utils.Captcha;
import com.newtouch.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private RedisUtil redisUtil;

    @GetMapping("/captcha")
    public TraderResponse<CaptchaResponse> captcha() throws Exception {
        // 1.生成验证码 120 40 4个字符 噪点+线条
        Captcha captcha = new Captcha(120, 40, 4, 10);

        // 2.将验证码<ID,验证码数值>放入缓存
        String uuid = IdWorker.getIdStr();
        redisUtil.addCache(uuid, captcha.getCode(), CacheEnum.CAPTCHA);

        // 3.使用base64编码图片，并返回给前台
        CaptchaResponse captchaResponse = CaptchaResponse.builder()
                .id(uuid).imageBase64(captcha.getBase64ByteStr())
                .build();

        return new TraderResponse<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), captchaResponse);
    }
}
