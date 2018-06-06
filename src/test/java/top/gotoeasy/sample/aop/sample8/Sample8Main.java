package top.gotoeasy.sample.aop.sample8;

import top.gotoeasy.framework.aop.EnhanceBuilder;
import top.gotoeasy.framework.core.config.DefaultConfig;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class Sample8Main {

    private static final Log log = LoggerFactory.getLogger(Sample8Main.class);

    public static void main(String[] args) {
        DefaultConfig.getInstance().set("log.level.trace", "1");
        Sample8AopAround aop = new Sample8AopAround();
        Sample8Bean enhance = EnhanceBuilder.get().setSuperclass(Sample8Bean.class).matchAop(aop).build();

        String rs = enhance.hello("world");
        log.info("被AOP拦截修改后的最终结果:{}", rs);

        enhance.hello2("world", "world2");
    }

}
