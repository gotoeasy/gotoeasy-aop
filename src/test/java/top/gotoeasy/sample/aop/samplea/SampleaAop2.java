package top.gotoeasy.sample.aop.samplea;

import top.gotoeasy.framework.aop.AopContext;
import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.After;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class SampleaAop2 {

    private static final Log log = LoggerFactory.getLogger(SampleaAop2.class);

    @Around(value = "*.compute(*)", classes = SampleaBean.class)
    public Object around1(SuperInvoker superInvoker, Object ... args) {
        log.info("around2-1 ......... 计算结果+1");
        return (double)superInvoker.invoke(args) + 1;
    }

    @After(value = "*.compute(*)", classes = SampleaBean.class)
    public void after(AopContext context) {
        log.info("计算结果 ......... {}", context.getResult());
    }
}
