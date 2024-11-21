package com.systex.gateway.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.systex.gateway.model.Result;

public class JsonUtil {
    public static String parseToJson(Result result){
        String jsonResponse;
        try {
            jsonResponse = new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            jsonResponse = "{\"code\":500,\"message\":\"JSON Process Error\"}";
        }
        return jsonResponse;
    }
}
