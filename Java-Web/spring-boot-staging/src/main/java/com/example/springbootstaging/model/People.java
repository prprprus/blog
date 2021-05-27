package com.example.springbootstaging.model;

import lombok.Data;

@Data
public class People {
    private String name;
    private int age;
    private String sex;

    public People(String name, int age, String sex) {
        this.name = name;
        this.age = age;
        this.sex = sex;
    }

    @Override
    public String toString() {
        return String.format("[name: %s, age: %s, sex: %s]", this.name, this.age, this.sex);
    }
}
