package top.gotoeasy.framework.aop.config;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.AopContext;
import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.After;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class Sample99AopAfter {

    private static final Log log = LoggerFactory.getLogger(Sample99AopAfter.class);

    // 拦截参数之最多
    @After("*.Sample99Bean.add(*)")
    public void after1(Enhance enhance, Method method, AopContext aopContext, SuperInvoker superInvoker, Exception ex, Object ... args) {
        log.debug("@After {}, 拦截{}", "拦截参数之最多, 耗时{}MS", method.getName(), System.currentTimeMillis() - aopContext.getStartTime());
    }

    @After("*.Sample99Bean.add(*)")
    public void after2() {
        log.debug("@After {}", "拦截参数之最少");
    }

    @After(value = "*.Sample99BaseBean.init()", annotations = Deprecated.class)
    public void after3(Enhance enhance, Method method) {
        log.warn("父类方法默认拦不到的");
    }

    @After(value = "*.Sample99BaseBean.init()", annotations = Deprecated.class, matchSuperMethod = true)
    public void after4(Enhance enhance) {
        log.debug("拦截父类方法要指定matchSuperMethod=true");
    }

    @After(value = "*.Object.toString()", matchSuperMethod = true, matchToString = true, order = 1)
    public void after5(Method method) {
        log.debug("拦截toString()");
    }

    @After(value = "*.Object.equals(java.lang.Object)", matchSuperMethod = true, matchEquals = true, order = 2)
    public void after6(Method method, AopContext aopContext, Object ... args) {
        log.debug("拦截equals(java.lang.Object)");
    }

    @After(value = "*.Object.hashCode()", matchSuperMethod = true, matchHashCode = true, order = 3)
    public void after7(AopContext aopContext, SuperInvoker superInvoker, Exception ex, Method method, Enhance enhance, Object ... args) {
        log.debug("拦截hashCode()");
    }

    @After(value = "*.Sample99*Bean.add(*)", matchSuperMethod = true)
    public void after8(Object ... args) {
        log.debug("@After {}, 子类父类方法一起拦截");
    }

}
