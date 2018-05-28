package top.gotoeasy.sample.aop.samplea;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class SampleaBean {

    private static final Log log = LoggerFactory.getLogger(SampleaBean.class);

    public double compute(int a, int b) {
        double rs = a / b;
        log.debug("计算结果：{}/{} = {}", a, b, rs);
        return rs;
    }

    public void print(int a) {
        log.debug("print {}", a);
    }

    public void print2(int a) {
        log.debug("print2 {}", a);
    }
}
