package com.yw.dynamicthreadpooladmin.types;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 响应类
 *
 * @author: yuanwen
 * @since: 2024/9/20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response<T> implements Serializable {

    private static final long serialVersionUID = -2474596551402989285L;

    private String code;

    private String msg;

    private T data;

    public Response(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Getter
    @AllArgsConstructor
    public enum CodeEnum {
        SUCCESS("0000", "调用成功"),
        ERROR("0001", "调用失败");

        private final String code;
        private final String msg;
    }


    public static <T> Response<T> success(T data) {
        return new Response<>("0000", "调用成功", data);
    }

    public static <T> Response<T> error(CodeEnum code) {
        return new Response<>(code.getCode(), code.getMsg());
    }
}
