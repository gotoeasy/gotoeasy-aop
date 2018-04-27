package top.gotoeasy.sample.aop.sample4;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Last;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class Sample4AopLast {

    private static final Log log = LoggerFactory.getLogger(Sample4AopLast.class);

    @Last("*.Sample4Bean.*")
    public void last(Enhance enhance, Method method, Object ... args) {
        log.info("  @Last   拦截{}", method.getName());
    }

}
