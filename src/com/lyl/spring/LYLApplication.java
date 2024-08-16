package com.lyl.spring;

import java.beans.Introspector;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class LYLApplication {
    private Class configClass;
    private final ConcurrentHashMap<String, BeanDefinition> beanDefinitionConcurrentHashMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> singletonObject = new ConcurrentHashMap<>();

    public LYLApplication(Class configClass) {
        this.configClass = configClass;

        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            String scanPath = componentScanAnnotation.value();
            ClassLoader classLoader = LYLApplication.class.getClassLoader();
            URL resource = classLoader.getResource(scanPath.replace(".", "/"));
            File file = new File(resource.getFile());
            System.out.println(file);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    String absolutePath = f.getAbsolutePath();
                    try {
                        if (absolutePath.endsWith(".class")) {
                            String className = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"));
                            Class<?> clazz = classLoader.loadClass(className.replace("\\", "."));
                            if (clazz.isAnnotationPresent(Component.class)) {
                                //知道这里定义了一个Bean
                                //但是这个时候并不会去创建,因为单例和多例的bean的创建时间不一样
                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setType(clazz);
                                if (clazz.isAnnotationPresent(Scope.class)) {
                                    Scope scope = clazz.getAnnotation(Scope.class);
                                    beanDefinition.setScope(scope.value());
                                } else {
                                    beanDefinition.setScope("singleton");
                                }
                                Component component = clazz.getAnnotation(Component.class);
                                String beanName = component.value();
                                if (beanName.isEmpty()) {
                                    beanName = Introspector.decapitalize(clazz.getSimpleName());
                                }
                                beanDefinitionConcurrentHashMap.put(beanName, beanDefinition);
                            }

                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        for (String beanName : beanDefinitionConcurrentHashMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionConcurrentHashMap.get(beanName);
            if ("singleton".equals(beanDefinition.getScope())) {
                Object bean = creatBean(beanName, beanDefinition);
                singletonObject.put(beanName, bean);
            }
        }
    }

    private Object creatBean(String beanName, BeanDefinition beanDefinition) {

        Class clazz = beanDefinition.getType();
        try {
            Object bean = clazz.getConstructor().newInstance();
            //依赖注入
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    field.set(bean, getBean(field.getName()));
                }
            }
            return bean;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public LYLApplication() {
    }

    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionConcurrentHashMap.get(beanName);
        if ("singleton".equals(beanDefinition.getScope())) {
            Object bean = singletonObject.get(beanName);
            if (bean == null) {
                bean = creatBean(beanName, beanDefinition);
                singletonObject.put(beanName, bean);
            }
            return bean;
        } else {
            return creatBean(beanName, beanDefinition);
        }
    }
}
