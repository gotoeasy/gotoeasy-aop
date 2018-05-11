package top.gotoeasy.framework.aop.config;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class Sample99BeanConstructor5 {

    private static final Log log   = LoggerFactory.getLogger(Sample99BeanConstructor5.class);

    private int              total = 0;

    public Sample99BeanConstructor5(String[] strings, int val) {
        log.debug("Sample99BeanConstructor5");
    }

    public int add(int intVal) {
        total += intVal;
        return total;
    }

}
