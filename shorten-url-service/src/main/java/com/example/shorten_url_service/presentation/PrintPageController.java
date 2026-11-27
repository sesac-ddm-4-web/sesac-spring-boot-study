package com.example.shorten_url_service.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PrintPageController {

    @GetMapping("/")
    public String showPrintPage() {
        return "forward:/print/index.html";
    }
}
