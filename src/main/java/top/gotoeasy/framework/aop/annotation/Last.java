package top.gotoeasy.framework.aop.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于声明一个最终拦截处理，在finally时执行
 * <p>
 * 相应类有@Aop声明才能被自动扫描<br>
 * 同一方法上可用多个@Last声明拦截对象，相互之间为“或”的关系<br>
 * </p>
 * <p>
 * 【注】<br>
 * 拦截编程需要非常清楚的知道所要拦截的目标方法，避免多拦截或漏拦截而产生问题，特别是目标程序经常修改的情况下<br>
 * 本拦截模块的初衷之一，是使用注解的方式来提升自由度，接口已经不是必须的了<br>
 * 换言之，在享受方法名参数等书写自由及性能提升的同时，需要自行对方法参数的正确性负责，即使本拦截模块会有必要的检查<br>
 * </p>
 * <p>
 * 【拦截处理方法中安全的参数写法】 (Enhance enhance, Method method, AopContext context, Object ... args)<br>
 * Last拦截处理方法不应该有返回值，即使写了也不会起作用<br>
 * </p>
 * 
 * @since 2018/03
 * @author 青松
 */
@Repeatable(Lasts.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Last {

    /**
     * 要拦截的方法名，支持通配符(*代表0或多个任意字符，?代表1个任意字符)，默认为*全部
     * 
     * @return 要拦截的方法名
     */
    String value() default "*";

    /**
     * 指定拦截目标所属类的包名范围
     * <p>
     * 默认空白不限定包名范围<br>
     * 多个包时用逗号分隔
     * </p>
     * 
     * @return 包名范围
     */
    String packages() default "";

    /**
     * 指定拦截目标的类注解
     * <p>
     * 默认Annotation注解，即不限定类注解
     * </p>
     * 
     * @return 类注解
     */
    Class<? extends Annotation>[] typeAnnotations() default Annotation.class;

    /**
     * 指定拦截目标的类
     * <p>
     * 默认void即指定类
     * </p>
     * 
     * @return 类
     */
    Class<?>[] classes() default void.class;

    /**
     * 指定目标方法需带的注解，默认Annotation注解，即不指定
     * 
     * @return 指定目标方法需带的注解
     */
    Class<? extends Annotation>[] annotations() default Annotation.class;

    /**
     * 最终拦截的执行顺序
     * <p>
     * 同一方法有多个最终拦截时，按此排序属性升序执行<br>
     * 默认为100，不修改则按无序执行，可通过此排序属性调整执行顺序
     * </p>
     * 
     * @return 序号
     */
    int order() default 100;

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
