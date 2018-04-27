package top.gotoeasy.sample.aop.sample4;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Throwing;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class Sample4AopThrowing {

    private static final Log log = LoggerFactory.getLogger(Sample4AopThrowing.class);

    @Throwing("*.Sample4Bean.*")
    public void throwing(Enhance enhance, Method method, Object ... args) {
        log.info("@Throwing 拦截{}", method.getName());
    }

}
