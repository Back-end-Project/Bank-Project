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
        return new ResponseEntity<>("Welcome to our Bank APP! - This endpoint is for connectivity testing. Use Postman to enjoy the app - Swagger page is in work - Nicholas & Hector", HttpStatus.CREATED);
    }
}
