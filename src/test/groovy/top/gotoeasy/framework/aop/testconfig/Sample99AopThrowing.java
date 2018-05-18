package top.gotoeasy.framework.aop.testconfig;

import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Throwing;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class Sample99AopThrowing {

    private static final Log log = LoggerFactory.getLogger(Sample99AopThrowing.class);

    private Exception        ex;

    @Throwing("*.Sample99Bean.mod(*)")
    public void throwing(Exception ex) {
        log.debug("@Throwing 拦截成功:{}", ex);
        this.ex = ex;
    }

    @Throwing(value = "*Sample99Bean.count2(*)", matchSuperMethod = true, matchToString = true)
    public void last2(SuperInvoker superInvoker, Exception ex, Object ... objects) {
        log.debug("@Throwing 拦截成功:{}", ex);
    }

    @Throwing(value = "*Sample99Bean.count2(*)", matchSuperMethod = true, matchToString = true)
    public void last2(Object ... objects) {
        log.debug("@Throwing 拦截成功");
    }

    public Exception getException() {
        return ex;
    }
}
