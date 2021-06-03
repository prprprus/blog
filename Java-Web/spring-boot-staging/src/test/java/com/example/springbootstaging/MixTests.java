package com.example.springbootstaging;

import org.junit.jupiter.api.Test;

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
        new MixTests.InnerClass();
        new MixTests.StaticInnerClass();

        System.out.println(Day.MONDAY);
        System.out.println(Day.SUNDAY);

        System.out.println(ErrorCodeEn.OK);
        System.out.println(ErrorCodeEn.OK.code);
        System.out.println(ErrorCodeEn.OK.msg);
    }

}