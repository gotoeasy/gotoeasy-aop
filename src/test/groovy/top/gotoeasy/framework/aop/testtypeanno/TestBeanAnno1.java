package top.gotoeasy.framework.aop.testtypeanno;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@MyTypeAnno1
public class TestBeanAnno1 {

    private static final Log log = LoggerFactory.getLogger(TestBeanAnno1.class);

    public String hello(String name) {
        log.debug("Hello {}", name);
        return "Hello " + name;
    }

}
