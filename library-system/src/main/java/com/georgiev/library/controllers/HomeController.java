package com.georgiev.library.controllers;

import com.georgiev.library.services.impl.UserServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;


@RestController
public class HomeController {
    private UserServiceImpl userService;
    private String[] admins = {"AccountThesis"};
    public HomeController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public ModelAndView HomeRedirect() {
        return new ModelAndView("home.html");
    }
    @GetMapping("/admin")
    public ModelAndView adminRedirect() {
        if(userService.getUser() != null){
            if(Arrays.asList(admins).contains(userService.getUser().getUsername())){
            return new ModelAndView("admin/dashboard.html");}
        }
        return new ModelAndView("home.html");
    }


}
