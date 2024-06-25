package com.newtouch.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraderResponse<T> {

    int code;

    String msg;

    T data;

    public TraderResponse<T> success(int code, String msg, T data) {
        return new TraderResponse<>(code, msg, data);
    }

    public TraderResponse<T> fail(int code, String msg, T data) {
        return new TraderResponse<>(code, msg, data);
    }
}
