package com.jobradar.application.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping(value ={"/", "/login", "/register", "/dashboard"})
    public String forward(){
        return "forward:/index.html";
    }

    @GetMapping("/verify-email")
    public String verifyEmailPage() {
        return "forward:/index.html";
    }

}
