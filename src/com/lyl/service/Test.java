package com.lyl.service;


import com.lyl.spring.LYLApplication;

public class Test {
    public static void main(String[] args) {
        LYLApplication lylApplication = new LYLApplication(AppConfig.class);
//        System.out.println((UserService) lylApplication.getBean("userService"));
//        System.out.println((UserService) lylApplication.getBean("userService"));
//        System.out.println((UserService) lylApplication.getBean("userService"));
//        System.out.println((UserService) lylApplication.getBean("userService"));
        UserInterface userService=(UserInterface) lylApplication.getBean("userService");
        userService.test();

        System.out.println((OrderService) lylApplication.getBean("orderService"));

    }
}
