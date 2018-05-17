package top.gotoeasy.testconfig;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class Sample99BeanConstructor3 {

    private static final Log log   = LoggerFactory.getLogger(Sample99BeanConstructor3.class);

    private int              total = 0;

    public Sample99BeanConstructor3(int val, String ... strings) {
        log.debug("Sample99BeanConstructor3,{},{}", val, strings);
    }

    public int add(int intVal) {
        total += intVal;
        return total;
    }

}
