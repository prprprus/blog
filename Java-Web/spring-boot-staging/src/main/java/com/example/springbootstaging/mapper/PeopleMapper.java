package com.example.springbootstaging.mapper;

import java.util.List;

import com.example.springbootstaging.model.People;

public interface PeopleMapper {
    List<People> getALL();
    People getOne(long id);
    void insert(People people);
    void update(People people);
    void delete(long id);
}
