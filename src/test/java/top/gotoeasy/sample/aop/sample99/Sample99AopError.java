package top.gotoeasy.sample.aop.sample99;

import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;

@Aop
public class Sample99AopError {

    @Around(value = "*Sample99BeanErr.sum(*)")
    public Object around(Object ... args) {
        return 1;
    }

}
