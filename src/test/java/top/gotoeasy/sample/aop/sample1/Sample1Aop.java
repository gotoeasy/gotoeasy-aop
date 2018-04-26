package top.gotoeasy.sample.aop.sample1;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Before;

@Aop
public class Sample1Aop {

    private int count;

    @Before("*.Sample1Add.add(*)")
    public void before(Enhance enhance, Method method, int val) {
        System.err.println("[Sample1Aop]Before add " + val);
        count++;
    }

    public int getCount() {
        return count;
    }
}
