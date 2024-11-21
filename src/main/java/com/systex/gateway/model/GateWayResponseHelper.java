package com.systex.gateway.model;

import com.systex.gateway.utils.JsonUtil;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import java.nio.charset.StandardCharsets;

public class GateWayResponseHelper {
    /***
     * 用來產程filter中錯誤到response中
     * @param httpStatus 要返回的httpStatus
     * @param code 錯誤代碼
     * @param message 錯誤訊息
     * @param response 當前的ServerWebExchange
     * @return DataBuffer
     */
    public static DataBuffer parseToDataBuffer(HttpStatus httpStatus, int code, String message, ServerHttpResponse response){
        response.setStatusCode(httpStatus);
        Result<Object> errorResult = Result.error(code, message);
        String jsonResponse = JsonUtil.parseToJson(errorResult);
        DataBuffer buffer = response.bufferFactory().wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));
        return  buffer;
    }

}
