package com.kantar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class IndexController {
    @GetMapping("/")
    public @ResponseBody String index(HttpServletRequest req, Model mdm){
        return "hello~";
    }
}
