package top.gotoeasy.sample.aop.sample99;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;
import top.gotoeasy.framework.aop.annotation.Before;

@Aop
public class Sample99AopCheck2 {

    @Before(value = "*Sample99Bean.isReady()")
    public void before(Enhance enhance, Method method, SuperInvoker superInvoker, Object ... args) {
    }

    @Around(value = "*Sample99Bean.isReady()")
    public Object around(SuperInvoker superInvoker, Object ... args) {
        return superInvoker.invoke(args) + ".";
    }
}
