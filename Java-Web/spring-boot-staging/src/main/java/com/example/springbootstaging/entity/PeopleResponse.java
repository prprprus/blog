package com.example.springbootstaging.entity;

import java.util.List;

import lombok.Data;
import org.springframework.stereotype.Component;

import com.example.springbootstaging.model.People;

@Data
@Component
public class PeopleResponse {
    private int code;
    private String message;
    private List<People> data;
}
