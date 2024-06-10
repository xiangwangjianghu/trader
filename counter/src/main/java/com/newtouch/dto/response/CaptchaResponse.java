package com.newtouch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CaptchaResponse {
    String captchaKey;

    String imageBase64;
}
