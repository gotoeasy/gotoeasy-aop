package top.gotoeasy.framework.aop.test21;

import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class Test21Aop {

    private static final Log log = LoggerFactory.getLogger(Test21Bean.class);

    @Around("*.compute(*)")
    public Object around1(SuperInvoker superInvoker, Object ... args) {
        log.debug("around1 .....");
        return superInvoker.invoke(args);
    }

    @Around("*.compute(*)")
    public Object around2(SuperInvoker superInvoker, Object ... args) {
        log.debug("around2 .....");
        return superInvoker.invoke(args);
    }

    @Around("*.print(*)")
    public Object around3(SuperInvoker superInvoker, Object ... args) {
        log.debug("around3 .....");
        return superInvoker.invoke(args);
    }

}
