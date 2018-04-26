package top.gotoeasy.sample.aop.sample3;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Before;

@Aop
public class Sample3Aop {

    private int count;

    @Before("*.add(*)")
    public void before(Enhance enhance, Method method, int val) {
        count++;
    }

    public int getCount() {
        return count;
    }
}
