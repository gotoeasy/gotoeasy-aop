package top.gotoeasy.framework.aop.testmethod;

import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;

@Aop
public class MethodAopAround {

    @Around("top.gotoeasy.framework.aop.method.MethodDesc.*")
    public Object around(SuperInvoker superInvoker, Object ... args) {
        return superInvoker.invoke(args);
    }

}
