package top.gotoeasy.framework.aop.config;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class Sample99BeanConstructor4 {

    private static final Log log   = LoggerFactory.getLogger(Sample99BeanConstructor4.class);

    private int              total = 0;

    public Sample99BeanConstructor4(String ... strings) {
        log.debug("Sample99BeanConstructor4");
    }

    public int add(int intVal) {
        total += intVal;
        return total;
    }

}
