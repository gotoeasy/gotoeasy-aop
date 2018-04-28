package top.gotoeasy.sample.aop.sample5;

import java.util.List;
import java.util.Map;

import top.gotoeasy.framework.aop.EnhanceBuilder;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class Sample5Main {

    private static final Log log = LoggerFactory.getLogger(Sample5Main.class);

    public static void main(String[] args) {
        Sample5AopEdit aop = new Sample5AopEdit();
        Sample5Bean enhance = (Sample5Bean)EnhanceBuilder.get().setSuperclass(Sample5Bean.class).matchAop(aop).build();

        List<Map<String, Object>> list = enhance.getData();
        log.info("被AOP拦截修改后的最终结果:{}", list);

        enhance.doNothing();
    }

}
