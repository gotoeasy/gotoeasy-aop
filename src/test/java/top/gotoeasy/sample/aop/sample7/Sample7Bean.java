package top.gotoeasy.sample.aop.sample7;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class Sample7Bean {

    private static final Log log = LoggerFactory.getLogger(Sample7Bean.class);

    public void hello(String name) {
        log.debug("Hello {}", name);
    }

}
