package com.lyl.service;

import com.lyl.spring.Autowired;
import com.lyl.spring.Component;
import com.lyl.spring.Scope;

@Component
@Scope("singleton")
public class UserService {

    @Autowired
    private OrderService orderService;

    public void test(){
        System.out.println(orderService);
        //


    }
}
