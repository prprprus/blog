package com.example.springbootstaging.mapper;

import java.util.List;

import com.example.springbootstaging.model.User;

public interface UserMapper {
    List<User> getALL();
    User getOne(long id);
    void insert(User User);
    void update(User User);
    void delete(long id);
}
