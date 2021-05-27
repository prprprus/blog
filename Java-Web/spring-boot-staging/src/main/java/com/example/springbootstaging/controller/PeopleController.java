package com.example.springbootstaging.controller;

import java.util.ArrayList;
import java.util.List;

import com.example.springbootstaging.enums.PeopleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.springbootstaging.entity.PeopleResponse;
import com.example.springbootstaging.model.People;

@RestController
@RequestMapping("/api/staging/people")
public class PeopleController {
    @Autowired
    private PeopleResponse peopleResponse;

    @GetMapping(value = "/name/{id}")
    public PeopleResponse getPeople(@PathVariable(value = "id", required = true) Integer id) {
        peopleResponse.setCode(0);
        peopleResponse.setMessage("success");

        // mock db select
        People p = new People(1, "yoko", 23, "female");
        List<People> data = new ArrayList<>();
        data.add(p);
        peopleResponse.setData(data);

        return peopleResponse;
    }

    @PostMapping("/upsert")
    public PeopleResponse upsertPeople(@RequestParam(value = "name", required = true) String name,
                                    @RequestParam(value = "age", required = true) Integer age,
                                    @RequestParam(value = "sex", required = true, defaultValue = "female") String sex) {
        // mock db update|insert
        People p = new People(1, "yoko", 23, "female");
        System.out.println(p);
        
        peopleResponse.setCode(0);
        peopleResponse.setMessage("success");
        return peopleResponse;
    }
}
