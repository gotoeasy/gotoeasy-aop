package top.gotoeasy.framework.aop.config;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class Sample99BeanConstructor {

    private static final Log log   = LoggerFactory.getLogger(Sample99BeanConstructor.class);
    private Sample99Bean     sample99Bean;
    private int              total = 0;

    public Sample99BeanConstructor(Sample99Bean sample99Bean, Sample99Bean sample99Bean2) {
        this.sample99Bean = sample99Bean;
        log.debug("{}, {}", sample99Bean, sample99Bean2);
    }

    public int add(int intVal) {
        total += intVal;
        return total;
    }

    public Sample99Bean getSample99Bean() {
        return sample99Bean;
    }

}
