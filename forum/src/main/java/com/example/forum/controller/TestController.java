package com.example.forum.controller;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/forum/home")
public class TestController {
    @GetMapping("/secured")
    public String secured(){
        return "welcome after logging in";
    }

    @GetMapping("/debug-auth")
    public void debugAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Principal type: " + auth.getPrincipal().getClass().getName());
        System.out.println("Principal value: " + auth.getPrincipal());
        System.out.println("Authorities: " + auth.getAuthorities());
    }

}
