package com.example.springbootstaging.controller;

import java.util.ArrayList;
import java.util.List;

import com.example.springbootstaging.enums.UserEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.springbootstaging.entity.UserResponse;
import com.example.springbootstaging.model.User;

@RestController
@RequestMapping("/api/staging/user")
public class UserController {
    @Autowired
    private UserResponse UserResponse;

    @GetMapping(value = "/name/{id}")
    public UserResponse getUser(@PathVariable(value = "id", required = true) Integer id) {
        UserResponse.setCode(0);
        UserResponse.setMessage("success");

        // mock db select
        User p = new User(1, "yoko", 23, "female");
        List<User> data = new ArrayList<>();
        data.add(p);
        UserResponse.setData(data);

        return UserResponse;
    }

    @PostMapping("/upsert")
    public UserResponse upsertUser(@RequestParam(value = "name", required = true) String name,
                                    @RequestParam(value = "age", required = true) Integer age,
                                    @RequestParam(value = "sex", required = true, defaultValue = "female") String sex) {
        // mock db update|insert
        User p = new User(1, "yoko", 23, "female");
        System.out.println(p);
        
        UserResponse.setCode(0);
        UserResponse.setMessage("success");
        return UserResponse;
    }
}
