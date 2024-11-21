package com.systex.gateway.model;

import lombok.Data;

import java.io.Serializable;
@Data
public class Result<T> implements Serializable {
    private Integer code; // 狀態碼
    private String message; //錯誤訊息
    private T data; // 數據
    public static <T> Result<T> success(){
        Result<T> result = new Result<T>();
        result.code=200;
        result.message="success";
        return result;
    }
    public static <T> Result<T> success(T obj){
        Result<T> result = new Result<>();
        result.code = 200;
        result.data = obj;
        return result;
    }

    public  static <T>Result<T> error(int code,String message){
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }
}
