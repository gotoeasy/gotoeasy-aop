package top.gotoeasy.framework.aop.testpackages.p2;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class TestBeanP2 {

    private static final Log log = LoggerFactory.getLogger(TestBeanP2.class);

    public String hello(String name) {
        log.debug("Hello {}", name);
        return "Hello " + name;
    }

}
