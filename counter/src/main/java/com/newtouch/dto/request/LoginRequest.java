package com.newtouch.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Long uid;

    @NotNull
    String password;

    @NotNull
    String captcha;

    @NotNull
    String captchaKey;

    String newPassword;
}
