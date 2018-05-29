package top.gotoeasy.framework.aop.test20;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;
import top.gotoeasy.framework.aop.annotation.Before;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class Test20Aop {

    private static final Log log = LoggerFactory.getLogger(Test20Bean.class);

    @Around
    public Object around(SuperInvoker superInvoker, Object ... args) {
        log.debug("around .....");
        return superInvoker.invoke(args);
    }

    @Before
    public void before(Method method) {
        log.debug("before .....");
    }
}
