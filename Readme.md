容器启动
BeanDefinition扫描
Bean的生命周期
单例和多例bean
依赖注入
AOP
Aware回调
初始化
BeanPostProcessor

------------------------------------------------------
# spring 底层原理

## 1.Bean生命周期

UserService.class----->无参构造方法(推断构造方法)------>普通对象----->依赖注入--------->初始化前(@PostConstruct)---->初始化------>初始化后(AOP)-------->代理对象---->放入单例池(就是一个map)----------->Bean对象



## 2.推断构造方法

当一个类中有多个构造方法,

Spring会判断这些构造方法中有没有无参构造,如果没有无参构造,那么会报错

```
@Component
@Scope("singleton")
public class UserService  {

    private OrderService orderService;

//如果有无参构造会优先使用无参构造
    public UserService() {
    }

    public UserService(OrderService orderService) {
        this.orderService = orderService;
    }
    public UserService(OrderService orderService,OrderService orderService1) {
        this.orderService = orderService;
    }
```

```
@Component
@Scope("singleton")
public class UserService  {

    private OrderService orderService;
//有多个有参构造时,并且没有告诉Spring使用哪个构造方法,那么Spring会报错
//可以使用@Autowired注解告诉Spring使用哪个构造方法
    public UserService(OrderService orderService) {
        this.orderService = orderService;
    }
    public UserService(OrderService orderService,OrderService orderService1) {
        this.orderService = orderService;
    }
```

```
@Component
@Scope("singleton")
public class UserService  {

    private OrderService orderService;

//如果只有一个构造方法,spring会直接使用该构造方法,同时,这个OrderService参数是有值的
//这个对象是哪里来的呢?
//如果OrderService是一个Bean时候,
//单例时
//Spring会先去单例池中找,如果有直接使用,如果没有就会去创建orderService 这个Bean
//在单例池中怎么找到这个Bean呢? 通过类型(byType),但是单例池中如果有多个OrderService呢?(@Component会创建一个,配置类中@Bean也会创建这样就会有多个OrderService,创建出来的对象的名字是不一样的)
//这时候通过类型(byType)去查找Bean会找到多个Bean,那么该使用哪个呢?
//这个时候就会通过参数名字orderService进行查找
//这就是所谓的先byType再byName
//如果找到多个或者没找到就会报错

//创建时可能会发生循环依赖

//多例时
//Spring不会去单例池中找,直接创建orderService 这个Bean
    public UserService(OrderService orderService) {
        this.orderService = orderService;
    }
    
```



## 3.依赖注入

依赖注入实现需要两步,

1.找到需要注入的属性

2.赋值

```
//依赖注入
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    field.set(bean, getBean(field.getName()));
                }
            }
```

依赖注入也是通过先byType再byName

## 4.@PostConstruct

## 5.初始化前

## 6.初始化

## 7.初始化后

动态代理

## 8.AOP

![](image\Snipaste_2024-08-18_21-16-39.png)

当使用AOP时,生成的代理对象中需要依赖注入的orderService是没有值的(null)

因为生成初始化后生成代理对象之后并不会再次进行依赖注入,所以代理对象的orderService是没有值的

```
@Component
@Scope("singleton")
public class UserService implements BeanNameAware, InitializingBean, UserInterface {


    private String beanName;

    @Autowired
    private OrderService orderService;

    public void test() {
        System.out.println(orderService);
        //


    }
```

![](image/Snipaste_2024-08-18_21-24-59.png)

但是去进行打印的时候并不是null

![](image/img.png)



生成代理对象有两种方式CGlib和JDK自带的

```
//当执行tset的是其实是执行的普通对象的test.普通对象是进行过依赖注入的,所以orderService是有值的
class UserServiceProxy extends UserService{
  UserService target;//普通对象
  public void test(){
    //切面逻辑@Before
    //target.test;
  }
}
```

![](image/img_1.png)

## 9.Spring事务

```
//开启事务的时候,
class UserServiceProxy extends UserService{
  UserService target;//普通对象
  public void test(){
    //Spring事务切面逻辑
    //判断有没有@Transactional注解,有就开启事务
    //1.事务管理器新建一个数据库连接connect
    //2. connect.autocommit=false
    //target.test; //普通对象.test()   jdbctemplate sql1  sql2  
    //connect.commot() 或者connect.rollback()
  }
}
```

事务失效原理

```
@Transactional
public void test(){
  jdbcTemplate.execute("insert into t1 values (1,2)");
  a();
}

@Transactional(propagation=PROPAGATION.NEVER)
public void a(){
  jdbcTemplate.execute("insert into t1 values (2,2)");
}
```

按照写的逻辑是希望当执行test()方法是报错并进行回滚,

但是执行的结果并没有报错,两个sql都会执行成功这是为什么呢?

```
//开启事务的时候,
class UserServiceProxy extends UserService{
  UserService target;//普通对象
  public void test(){
    //Spring事务切面逻辑
    //判断有没有@Transactional注解,有就开启事务
    //1.事务管理器新建一个数据库连接connect
    //2. connect.autocommit=false
    //target.test; //普通对象.test() 当执行到test()时,回调用a()方法,但是是普通对象的a()方法,普通对象的a方法是不会有事务的, 所以执行结果并不会按照期望报错回滚
    //connect.commot() 或者connect.rollback()
  }
}
```

如果想要达到希望的结果,可以这样写

```
@Autowired
    private UserService userService;

@Transactional
public void test(){
  jdbcTemplate.execute("insert into t1 values (1,2)");
  userService.a();//这里就会使用代理对象的a()方法,事务就会生效
}

@Transactional(propagation=PROPAGATION.NEVER)
public void a(){
  jdbcTemplate.execute("insert into t1 values (2,2)");
}
```



事务传播机制

```
REQUIRED(0),
SUPPORTS(1),
MANDATORY(2),
REQUIRES_NEW(3),
NOT_SUPPORTED(4),
NEVER(5),有事务存在就抛异常
NESTED(6);
```

## 10.@Configuration

## 11.循环依赖

## 12.Spring整合Mybatis

