package top.gotoeasy.framework.aop.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于声明一个前置拦截处理，在调用指定方法前执行
 * <p>
 * 相应类必须同时有@Aop声明<br>
 * 仅对类自身定义的public方法有效
 * </p>
 * <p>
 * 【注】<br>
 * 拦截编程需要非常清楚的知道所要拦截的目标方法，避免多拦截或漏拦截而产生问题，特别是目标程序经常修改的情况下<br>
 * 本拦截模块的初衷之一，是使用注解的方式来提升自由度，接口已经不是必须的了<br>
 * 换言之，在享受方法名参数等书写自由及性能提升的同时，需要自行对方法参数的正确性负责，即使本拦截模块会有必要的检查<br>
 * </p>
 * <p>
 * 【拦截处理方法中最安全的参数写法】 (Enhancer enhancer, Method method, Object ... args)<br>
 * Before拦截处理方法不应该有返回值，即便写了也不会出错<br>
 * </p>
 * 
 * @since 2018/04
 * @author 青松
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Before {

    /**
     * 要拦截的方法名，支持通配符(*代表0或多个任意字符，?代表1个任意字符)，默认为*全部
     * 
     * @return 要拦截的方法名
     */
    String value() default "*";

    /**
     * 要拦截的带指定注解的方法，默认Aop类注解即不起作用
     * 
     * @return 要拦截的带指定注解的方法
     */
    Class<? extends Annotation> annotation() default Aop.class;

    /**
     * 是否要拦截父类方法
     * 
     * @return true:包含父类声明的方法/false:仅限于类自己声明的方法
     */
    boolean matchSuperMethod() default false;

    /**
     * 是否把equals()方法作为拦截匹配对象
     * 
     * @return true:作为拦截匹配对象/false:不作为拦截匹配对象
     */
    boolean matchEquals() default false;

    /**
     * 是否把toString()方法作为拦截匹配对象
     * 
     * @return true:作为拦截匹配对象/false:不作为拦截匹配对象
     */
    boolean matchToString() default false;

    /**
     * 是否把hashCode()方法作为拦截匹配对象
     * 
     * @return true:作为拦截匹配对象/false:不作为拦截匹配对象
     */
    boolean matchHashCode() default false;

}
