package top.gotoeasy.framework.aop.test19;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.AopContext;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Throwing;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class Test19Aop {

    private static final Log log = LoggerFactory.getLogger(Test19Bean.class);

    @Throwing
    public void throwing(Method method, AopContext context) {
        log.debug("throwing .....");
    }
}
