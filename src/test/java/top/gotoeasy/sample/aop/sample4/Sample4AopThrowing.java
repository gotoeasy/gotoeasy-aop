package top.gotoeasy.sample.aop.sample4;

import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Throwing;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class Sample4AopThrowing {

    private static final Log log = LoggerFactory.getLogger(Sample4AopThrowing.class);

    private Exception        ex;

    @Throwing("*.Sample4Bean.*")
    public void throwing(Exception ex, Object ... objects) {
        log.info("@Throwing 拦截{}", ex);
        this.ex = ex;
    }

    public Exception getException() {
        return ex;
    }

}
