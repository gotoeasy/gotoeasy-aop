package top.gotoeasy.testconfig;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class Sample99AopAround {

    private static final Log log = LoggerFactory.getLogger(Sample99AopAround.class);

    @Around(value = "*Sample99BaseBean.arount()", annotations = Deprecated.class, matchSuperMethod = true)
    public void around(Enhance enhance, Method method, SuperInvoker superInvoker, Object ... args) {
        log.info("@Around 拦截{}", method.getName());
        superInvoker.invoke(args);
    }

    @Around(value = "*Sample99BaseBean.hello(*)", matchSuperMethod = true, matchToString = true)
    public Object aroundHello(Enhance enhance, Method method, SuperInvoker superInvoker, Object ... args) {
        log.info("@Around 拦截{}", method.getName());
        return superInvoker.invoke(args);
    }

    @Around(value = "*Sample99BaseBean.arountVoid(*)", matchSuperMethod = true, matchToString = true)
    public Object arountVoid(Enhance enhance, Method method, SuperInvoker superInvoker, Object ... args) {
        log.info("@Around 拦截{}", method.getName());
        return superInvoker.invoke(args);
    }

    @Around("*Sample99Bean.sum*(*)")
    public Object arountSum(Enhance enhance, Method method, SuperInvoker superInvoker, Object ... args) {
        log.info("@Around 拦截{}", method.getName());
        return superInvoker.invoke(args);
    }

    @Around("*Sample99Bean.getTotal(*)")
    public Object arountGetTotal(Enhance enhance, Method method, SuperInvoker superInvoker, Object ... args) {
        log.info("@Around 拦截{}", method.getName());
        return superInvoker.invoke(args);
    }

    @Around("*Sample99Bean.getTotal2(*)")
    public Object arountGetTotal2(SuperInvoker superInvoker, String s1, String s2) {
        return superInvoker.invoke(s1, s2);
    }
}
