package com.softserve.bookstoreapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class MainPageController {

    @GetMapping("/main")
    public ResponseEntity<String> getMainPage(Principal principal) {
        return ResponseEntity.ok(principal.getName());
    }
}
