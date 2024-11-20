package com.systex.gateway.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class TestController {
    @GetMapping("/home")
    @ResponseBody
    public String testHome() {
        return "Hello World";
    }
    @GetMapping("/test")
    @ResponseBody
    public String testTest() {
        return "Hello testTest";
    }

}
