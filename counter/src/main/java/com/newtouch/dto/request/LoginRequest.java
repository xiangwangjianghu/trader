package com.newtouch.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {

    @NotNull
    String username;

    @NotNull
    String password;

    @NotNull
    String captcha;

    @NotNull
    String captchaKey;

    String newPassword;
}
