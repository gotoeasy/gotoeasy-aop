package top.gotoeasy.framework.aop.test18;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.annotation.Before;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class Test18Aop {

    private static final Log log = LoggerFactory.getLogger(Test18Bean.class);

    @Before
    public void before(Method method) {
        log.debug("before .....");
    }
}
