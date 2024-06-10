package com.newtouch.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseEnum {

    SUCCESS(200, "成功"),
    FAIL(400, "失败"),
    LOGIN_USER_ERROR(401, "用户名或密码错误"),
    LOGIN_CAPTCHA_ERROR(402, "验证码错误"),
    LOGIN_REDIRECT_ERROR(403, "请重新登陆"),
    UPDATE_PASSWORD_ERROR(404, "用户名或密码错误"),
    ;

    int code;

    String msg;
}
