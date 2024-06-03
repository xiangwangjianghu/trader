package com.newtouch.dto.response;

import lombok.Data;

@Data
public class CaptchaResponse {
    private String id;

    private String imageBase64;
}
