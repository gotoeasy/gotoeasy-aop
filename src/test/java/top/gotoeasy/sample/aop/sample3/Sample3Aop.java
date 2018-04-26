package top.gotoeasy.sample.aop.sample3;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;

@Aop
public class Sample3Aop {

    @Around("*Sample3Add*")
    public int around(Enhance enhance, Method method, SuperInvoker superInvoker, Object ... args) {
        return (int)superInvoker.invoke(args) + 1;
    }

}
