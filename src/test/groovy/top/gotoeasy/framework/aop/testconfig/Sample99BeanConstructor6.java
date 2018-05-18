package top.gotoeasy.framework.aop.testconfig;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class Sample99BeanConstructor6 {

    private static final Log log   = LoggerFactory.getLogger(Sample99BeanConstructor6.class);

    private int              total = 0;

    public Sample99BeanConstructor6(String[] strings) {
        log.debug("Sample99BeanConstructor6 {}", strings, "");
    }

    public int add(int intVal) {
        total += intVal;
        return total;
    }

}
