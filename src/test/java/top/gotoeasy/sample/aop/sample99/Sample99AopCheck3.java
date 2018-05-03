package top.gotoeasy.sample.aop.sample99;

import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;

@Aop
public class Sample99AopCheck3 {

    @Around(value = "*Sample99Bean.isReady()")
    public void around(SuperInvoker superInvoker, Object ... args) {
    }
}
