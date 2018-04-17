package top.gotoeasy.framework.aop.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于声明一个最终拦截处理，在finally时执行
 * <p/>
 * 相应类必须同时有@Aop声明<br/>
 * 仅对类自身定义的public方法有效
 * @since 2018/03
 * @author 青松
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Last {

    /** 要拦截的方法名，支持通配符(*代表0或多个任意字符，?代表1个任意字符)，默认为*全部 */
    String value() default "*";

    /** 要拦截的带指定注解的方法，默认Aop类注解即不起作用 */
    Class<? extends Annotation> annotation() default Aop.class;
}
