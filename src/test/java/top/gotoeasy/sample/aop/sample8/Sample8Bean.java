package top.gotoeasy.sample.aop.sample8;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class Sample8Bean {

    private static final Log log = LoggerFactory.getLogger(Sample8Bean.class);

    public String hello(String name) {
        return "Hello " + name;
    }

    public void hello2(String name, String name2) {
        log.debug("Hi {} {}", name, name2);
    }

}
