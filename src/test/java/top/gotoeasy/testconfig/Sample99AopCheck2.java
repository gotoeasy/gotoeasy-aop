package top.gotoeasy.testconfig;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;
import top.gotoeasy.framework.aop.annotation.Before;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class Sample99AopCheck2 {

    private static final Log log = LoggerFactory.getLogger(Sample99AopCheck2.class);

    @Before(value = "*Sample99Bean.isReady()")
    public void before(Enhance enhance, Method method, SuperInvoker superInvoker, Object ... args) {
        log.debug("@Before");
    }

    @Around(value = "*Sample99Bean.isReady()")
    public Object around(SuperInvoker superInvoker, Object ... args) {
        return superInvoker.invoke(args) + ".";
    }
}
