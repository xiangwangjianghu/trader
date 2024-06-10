package com.newtouch.handler;

import com.newtouch.dto.TraderResponse;
import com.newtouch.enums.ResponseEnum;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 全局异常捕获

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 传递格式不支持
     */
    @ExceptionHandler(BindException.class)
    public TraderResponse<String> validExceptionHandler(BindException bindException) {
        TraderResponse<String> result = new TraderResponse<>();
        return result.fail(ResponseEnum.FAIL.getCode(), bindException.getAllErrors().getFirst().getDefaultMessage(), null);
    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    @ResponseStatus(code = HttpStatus.METHOD_NOT_ALLOWED)
    public TraderResponse<String> handleException(HttpRequestMethodNotSupportedException e) {
        String msg = "不支持' " + e.getMethod() + "'请求";
        TraderResponse<String> result = new TraderResponse<>();
        return result.fail(ResponseEnum.FAIL.getCode(), msg, null);
    }

    /**
     * 未知运行时异常
     */
//    @ExceptionHandler(RuntimeException.class)
//    public TraderResponse<String> runtimeException(RuntimeException e) {
//        String msg = "运行时异常:" + e.getMessage();
//        TraderResponse<String> result = new TraderResponse<>();
//        return result.fail(ResponseEnum.FAIL.getCode(), msg, null);
//    }
}
