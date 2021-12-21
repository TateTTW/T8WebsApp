package com.t8webs.enterprise;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;

@Controller
public class T8WebsController {

    @RequestMapping("/")
    public String index(HttpServletResponse response) {
        return "start";
    }
}
