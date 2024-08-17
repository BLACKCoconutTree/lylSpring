package com.lyl.service;

import com.lyl.spring.BeanPostProcessor;
import com.lyl.spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class LylBeanPostProcessor implements BeanPostProcessor {


    @Override
    public Object postProcessorBeforeInitialization(String beanName, Object bean) {
        if ("userService".equals(beanName)) {
            //可以针对某个类进行处理
            System.out.println("初始化前");
        }
        return bean;
    }

    @Override
    public Object postProcessorAfterInitialization(String beanName, Object bean) {
        //可以生成代理对象
        if ("userService".equals(beanName)) {
            System.out.println("初始化后,生成代理对象");
            System.out.println("生成代理对象前" + bean);
            Object proxyInstance = Proxy.newProxyInstance(LylBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("切面逻辑");
                    return method.invoke(bean, args);
                }
            });
            System.out.println("生成代理对象后" + proxyInstance);
            return proxyInstance;
        }
        return bean;
    }
}
