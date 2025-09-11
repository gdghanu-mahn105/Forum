package com.example.forum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/forum/home")
@RequiredArgsConstructor
public class HomeController {

    @GetMapping
    public String secured(){
        return "hello, UNSECURED!";
    }


}
