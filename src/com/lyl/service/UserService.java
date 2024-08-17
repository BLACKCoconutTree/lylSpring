package com.lyl.service;

import com.lyl.spring.*;

@Component
@Scope("singleton")
public class UserService implements BeanNameAware , InitializingBean {


    private String beanName;

    @Autowired
    private OrderService orderService;

    public void test(){
        System.out.println(orderService);
        //


    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName=beanName;
    }

    @Override
    public void afterPropertiesSet() {
        //实现了InitializingBean这个接口之后就可以在spring创建了bean之后做一些事情
        //比如设置一些变量的参数
        System.out.println("初始化");
    }
}
