package com.example.springbootstaging.model;

import lombok.Data;

@Data
public class People {
    private long id;
    private String name;
    private int age;
    private String sex;

    public People(long id, String name, int age, String sex) {
        this.name = name;
        this.age = age;
        this.sex = sex;
    }

    @Override
    public String toString() {
        return String.format("[id: %s, name: %s, age: %s, sex: %s]", this.id, this.name, this.age, this.sex);
    }
}
