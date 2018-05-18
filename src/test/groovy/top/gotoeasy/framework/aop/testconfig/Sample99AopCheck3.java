package top.gotoeasy.framework.aop.testconfig;

import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class Sample99AopCheck3 {

    private static final Log log = LoggerFactory.getLogger(Sample99AopCheck3.class);

    @Around(value = "*Sample99Bean.isReady()")
    public void around(SuperInvoker superInvoker, Object ... args) {
        log.debug("@Around");
    }
}
