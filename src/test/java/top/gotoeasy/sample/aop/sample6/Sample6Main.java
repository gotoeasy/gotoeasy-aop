package top.gotoeasy.sample.aop.sample6;

import top.gotoeasy.framework.aop.EnhanceBuilder;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class Sample6Main {

    private static final Log log = LoggerFactory.getLogger(Sample6Main.class);

    public static void main(String[] args) {
        Sample6AopEdit aop = new Sample6AopEdit();
        Sample6Bean enhance = EnhanceBuilder.get().setSuperclass(Sample6Bean.class).matchAop(aop).build();

        String rs = enhance.hello("world");
        log.info("被AOP拦截修改后的最终结果:{}", rs);

    }

}
