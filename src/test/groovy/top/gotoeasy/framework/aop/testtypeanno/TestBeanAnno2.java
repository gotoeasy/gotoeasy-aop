package top.gotoeasy.framework.aop.testtypeanno;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@MyTypeAnno2
public class TestBeanAnno2 {

    private static final Log log = LoggerFactory.getLogger(TestBeanAnno2.class);

    public String hello(String name) {
        log.debug("Hello {}", name);
        return "Hello " + name;
    }

    public String hello(String name, int age) {
        log.debug("---------------- Hello {}, age={}", name, age);
        return "Hello " + name;
    }
}
