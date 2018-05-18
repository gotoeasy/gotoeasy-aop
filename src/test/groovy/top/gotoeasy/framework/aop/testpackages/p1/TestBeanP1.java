package top.gotoeasy.framework.aop.testpackages.p1;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class TestBeanP1 {

    private static final Log log = LoggerFactory.getLogger(TestBeanP1.class);

    public String hello(String name) {
        log.debug("Hello {}", name);
        return "Hello " + name;
    }

}
