package com.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Home {

    @GetMapping("/home")
    ResponseEntity<?> getHome(){
        System.out.println("Welcome HOME");
        return new ResponseEntity<>("You hit the endpoint!", HttpStatus.CREATED);
    }
}
