package top.gotoeasy.framework.aop.config;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class Sample99BeanConstructor2 {

    private static final Log log   = LoggerFactory.getLogger(Sample99BeanConstructor2.class);

    private int              total = 0;

    public Sample99BeanConstructor2() {
        log.debug("Sample99BeanConstructor2");
    }

    public int add(int intVal) {
        total += intVal;
        return total;
    }

}
