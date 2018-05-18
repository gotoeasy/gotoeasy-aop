package top.gotoeasy.framework.aop.testconfig;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.AopContext;
import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Before;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class Sample99AopBefore {

    private static final Log log = LoggerFactory.getLogger(Sample99AopBefore.class);

    // 拦截参数之最多
    @Before("*.Sample99Bean.add(*)")
    public void before1(Enhance enhance, Method method, AopContext aopContext, SuperInvoker superInvoker, Exception ex, Object ... args) {
        log.debug("@Before {}, 拦截{}", "拦截参数之最多", method.getName());
    }

    @Before("*.Sample99Bean.add(*)")
    public void before2() {
        log.debug("@Before {}", "拦截参数之最少");
    }

    @Before(value = "*.Sample99Bean.init()", annotations = Deprecated.class)
    public void before3(Enhance enhance, Method method) {
        log.warn("父类方法默认拦不到的");
    }

    @Before(value = "*.Sample99Bean.init()", annotations = Deprecated.class, matchSuperMethod = true)
    public void before4(Enhance enhance) {
        log.debug("拦截父类方法要指定matchSuperMethod=true");
    }

    @Before(value = "*.Sample99Bean.toString()", matchSuperMethod = true, matchToString = true, order = 1)
    public void before5(Method method) {
        log.debug("拦截toString()");
    }

    @Before(value = "*.Sample99Bean.equals(java.lang.Object)", matchSuperMethod = true, matchEquals = true, order = 2)
    public void before6(Method method, AopContext aopContext, Object ... args) {
        log.debug("拦截equals(java.lang.Object)");
    }

    @Before(value = "*.Sample99Bean.hashCode()", matchSuperMethod = true, matchHashCode = true, order = 3)
    public void before7(AopContext aopContext, SuperInvoker superInvoker, Exception ex, Method method, Enhance enhance, Object ... args) {
        log.debug("拦截hashCode()");
    }

    @Before(value = "*.Sample99Bean.add(*)", matchSuperMethod = true)
    public void before8(Object ... args) {
        log.debug("@Before {}, 子类父类方法一起拦截");
    }

}
