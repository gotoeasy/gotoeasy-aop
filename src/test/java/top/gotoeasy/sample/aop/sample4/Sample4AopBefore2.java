package top.gotoeasy.sample.aop.sample4;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Before;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class Sample4AopBefore2 {

    private static final Log log = LoggerFactory.getLogger(Sample4AopBefore2.class);

    @Before("*.Sample4Bean.*")
    public void before(Enhance enhance, Method method, Object ... args) {
        log.info("@Before 拦截{}", method.getName());
    }

}
