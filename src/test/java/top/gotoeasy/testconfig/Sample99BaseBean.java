package top.gotoeasy.testconfig;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class Sample99BaseBean {

    private static final Log log = LoggerFactory.getLogger(Sample99BaseBean.class);

    public String hello(String name) {
        return "Hello " + name;
    }

    public int add(String val1, String val2) {
        return Integer.valueOf(val1) + Integer.valueOf(val2);
    }

    @Deprecated
    public void init() {
        log.debug("@Deprecated");
    }

    @Deprecated
    public void arount(String val) {
        log.debug("@Deprecated");
    }

    public int count(String val1, String val2) {
        return Integer.valueOf(val1) + Integer.valueOf(val2);
    }

    public int count2(String val) {
        return 0;
    }

    public void arountVoid(String val) {
        log.debug("arountVoid");
    }
}
