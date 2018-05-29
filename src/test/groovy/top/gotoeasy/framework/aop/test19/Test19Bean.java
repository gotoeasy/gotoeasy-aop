package top.gotoeasy.framework.aop.test19;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class Test19Bean {

    private static final Log log = LoggerFactory.getLogger(Test19Bean.class);

    public int compute(int v1, int v2) {
        log.debug("compute({}, {})", v1, v2);
        return v1 / v2;
    }

}
