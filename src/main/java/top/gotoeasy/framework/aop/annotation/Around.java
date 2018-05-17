package top.gotoeasy.framework.aop.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于声明一个调用拦截处理，用以替换原方法的调用，一个方法只能有一个拦截生效
 * <p>
 * 相应类必须同时有@Aop声明<br>
 * 仅对类自身定义的public方法有效
 * </p>
 * <p>
 * 【注】<br>
 * 可以和@Before、@After、@Throwing、@Last共同拦截同一方法<br>
 * 但Around拦截是独占的，不能有多个@Around共同拦截同一方法
 * </p>
 * <p>
 * 【注】<br>
 * 拦截编程需要非常清楚的知道所要拦截的目标方法，避免多拦截或漏拦截而产生问题，特别是目标程序经常修改的情况下<br>
 * 本拦截模块的初衷之一，是使用注解的方式来提升自由度，接口已经不是必须的了<br>
 * 换言之，在享受方法名参数等书写自由及性能提升的同时，需要自行对方法参数的正确性负责，即使本拦截模块会有必要的检查<br>
 * </p>
 * <p>
 * 【Around拦截处理方法中最安全的参数写法】 (Enhance enhance, Method method, SuperInvoker superInvoker, Object ... args)<br>
 * 被拦截方法有返回值时，拦截处理方法若没有返回值将会发生编译异常<br>
 * 返回类型不一致无法强制转换时，将会发生编译异常<br>
 * 特别是同时拦截了多个方法，且不同方法的返回类型无法相互转换时，将会发生编译异常<br>
 * </p>
 * 
 * @since 2018/04
 * @author 青松
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Around {

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
