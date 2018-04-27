package top.gotoeasy.sample.aop.sample2;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;

@Aop
public class Sample2Aop {

    @Around("*Sample2Add.add*")
    public int around(Enhance enhance, Method method, SuperInvoker superInvoker, Object ... args) {
        return (int)superInvoker.invoke(args) + 1;
    }

    @Around("*Sample2Add.getTotal*")
    public int aroundGetTotal(SuperInvoker superInvoker) {
        return (int)superInvoker.invoke();
    }
}
