package top.gotoeasy.testconfig;

import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;

@Aop
public class Sample99AopError {

    @Around(value = "*Sample99BeanErr.sum(*)")
    public Object around(Object ... args) {
        return 1;
    }

}
