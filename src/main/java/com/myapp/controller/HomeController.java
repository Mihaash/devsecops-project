package com.myapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.Hidden;

@Controller
@Hidden
public class HomeController {

    @RequestMapping("/")
    public String home() {
        return "redirect:/swagger-ui/index.html";
    }

}
