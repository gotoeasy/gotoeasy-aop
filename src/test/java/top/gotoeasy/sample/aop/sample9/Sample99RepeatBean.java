package top.gotoeasy.sample.aop.sample9;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class Sample99RepeatBean {

    private static final Log log = LoggerFactory.getLogger(Sample99RepeatBean.class);

    @Deprecated
    public int sum(int intVal1, int intVal2) {
        int val = intVal1 + intVal2;
        log.debug("sum({}, {}) = {}", intVal1, intVal2, val);
        return val;
    }

    public int multiply(int intVal1, int intVal2) {
        int val = intVal1 * intVal2;
        log.debug("multiply({}, {}) = {}", intVal1, intVal2, val);
        return val;
    }
}
