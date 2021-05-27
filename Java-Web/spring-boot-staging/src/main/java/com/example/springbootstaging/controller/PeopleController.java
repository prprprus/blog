package com.example.springbootstaging.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.springbootstaging.entity.PeopleResponse;

@RestController
@RequestMapping("/api/staging/people")
public class PeopleController {
    @Autowired
    private PeopleResponse peopleResponse;

    @GetMapping(value = "/name/{id}")
    public PeopleResponse getName(@PathVariable(value = "id", required = true) Integer id) {
        peopleResponse.setCode(0);
        peopleResponse.setMessage("success");
        return peopleResponse;
    }

//    @PostMapping("/add")
//    public
}
