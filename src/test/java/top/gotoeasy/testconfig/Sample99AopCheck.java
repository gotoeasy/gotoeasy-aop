package top.gotoeasy.testconfig;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class Sample99AopCheck {

    private static final Log log = LoggerFactory.getLogger(Sample99AopCheck.class);

    @Around(value = "*Sample99Bean.isReady()")
    public Object around(Enhance enhance, Method method, SuperInvoker superInvoker, Object ... args) {
        log.info("@Around 拦截{}", method.getName());
        return superInvoker.invoke(args) + ".";
    }

    @Around(value = "*Sample99Bean.isReady()")
    public Object around(SuperInvoker superInvoker, Object ... args) {
        log.info("@Around 拦截{}", "hello2");
        return superInvoker.invoke(args) + ".";
    }
}
