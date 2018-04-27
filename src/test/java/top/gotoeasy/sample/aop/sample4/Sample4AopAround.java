package top.gotoeasy.sample.aop.sample4;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class Sample4AopAround {

    private static final Log log = LoggerFactory.getLogger(Sample4AopThrowing.class);

    @Around(annotation = Deprecated.class, matchSuperMethod = true)
    public void around(Enhance enhance, Method method, SuperInvoker superInvoker, Object ... args) {
        log.info("@Around 拦截{}", method.getName());
        superInvoker.invoke(args);
    }

    @Around(value = "*Sample4BaseBean.hello(*)", matchSuperMethod = true, matchToString = true)
    public Object aroundHello(Enhance enhance, Method method, SuperInvoker superInvoker, Object ... args) {
        log.info("@Around 拦截{}", method.getName());
        return superInvoker.invoke(args);
    }
}
