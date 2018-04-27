package top.gotoeasy.sample.aop.sample4;

import top.gotoeasy.framework.aop.EnhanceBuilder;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class Sample4Main {

    private static final Log log = LoggerFactory.getLogger(Sample4Main.class);

    public static void main(String[] args) {
        runSample4();
    }

    @SuppressWarnings("deprecation")
    public static void runSample4() {
        Sample4AopBefore aopBefore = new Sample4AopBefore();
        Sample4AopAfter aopAfter = new Sample4AopAfter();
        Sample4AopThrowing aopThrowing = new Sample4AopThrowing();
        Sample4AopLast aopLast = new Sample4AopLast();
        Sample4AopAround aopAround = new Sample4AopAround();

        Sample4Bean enhance = (Sample4Bean)EnhanceBuilder.get().setSuperclass(Sample4Bean.class)
                .matchAop(aopBefore, aopAfter, aopThrowing, aopLast, aopAround).build();

        enhance.init();
        enhance.initTotal();
        enhance.isReady();
        enhance.add(1);
        enhance.addAll(1, 2, 3, 4, 5);
        enhance.hello("AOP");
        log.info("Total={}", enhance.getTotal());

    }

}
