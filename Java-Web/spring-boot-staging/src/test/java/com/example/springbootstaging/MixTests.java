package com.example.springbootstaging;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class MixTests {
    private int flag = 1;
    static int flag2 = 2333;

    static {
        try {
            System.out.println("初始化类变量");
        } catch (Exception e) {
            System.out.println("初始化类变量异常");
        }
    }

    {
        try {
            System.out.println("初始化实例变量");
        } catch (Exception e) {
            System.out.println("初始化实例变量异常");
        }
    }

    private class InnerClass {
        //static int a = 1;
        static final int a = 1;
        private int b;

        {
            System.out.println("非静态嵌套类");
            System.out.println(flag);
            System.out.println(flag2);
        }
    }

    private static class StaticInnerClass {
        static int a = 1;
        private int b;

        {
            System.out.println("静态嵌套类");
            //System.out.println(flag);
            System.out.println(flag2);
        }
    }

    public enum Day {
        SUNDAY, MONDAY, TUESDAY, WEDNESDAY,
        THURSDAY, FRIDAY, SATURDAY
    }

    enum ErrorCodeEn {
        OK(0, "成功"),
        ERROR_A(100, "错误A"),
        ERROR_B(200, "错误B");

        private int code;
        private String msg;

        ErrorCodeEn(int number, String msg) {
            this.code = number;
            this.msg = msg;
        }
    }

    @Test
    void Case1() {
        //String s1 = "111";
        //String s2 = s1.intern();
        //String s3 = "111";
        //System.out.println(s1 == s2);
        //System.out.println(s1 == s3);

        String s1 = new String("aaa");
        String s2 = new String("aaa");
        String s3 = new String("aaa");
        System.out.println(s1 == s2);
        System.out.println(s1 == s3);
    }

}