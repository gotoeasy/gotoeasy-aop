package top.gotoeasy.sample.aop.samplea;

import top.gotoeasy.framework.aop.EnhanceBuilder;
import top.gotoeasy.framework.core.config.DefaultConfig;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class SampleaMain {

    private static final Log log = LoggerFactory.getLogger(SampleaMain.class);

    public static void main(String[] args) {
        DefaultConfig.getInstance().set("log.level.trace", "true");
        SampleaBean bean = EnhanceBuilder.get().setSuperclass(SampleaBean.class).matchAop(new SampleaAop1(), new SampleaAop2()).build();

        double rs = bean.compute(3, 2);
        log.debug("SampleaBean.compute(3, 2) = {}", rs);
        bean.print(1);

    }

}
