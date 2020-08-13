package com.lexiang.oauth.annotation;




import java.lang.annotation.*;

/**
 * @author 王乐
 * @apiNote  注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckUser {

    String[] roles() default {};

    Class<?> clazz() default Object.class;

    boolean check() default true;


}
