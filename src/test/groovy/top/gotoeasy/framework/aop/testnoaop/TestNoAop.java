package top.gotoeasy.framework.aop.testnoaop;

import top.gotoeasy.framework.aop.annotation.Before;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class TestNoAop {

    private static final Log log = LoggerFactory.getLogger(TestNoAop.class);

    @Before
    public void before() {
        log.info("before .........");
    }

}
