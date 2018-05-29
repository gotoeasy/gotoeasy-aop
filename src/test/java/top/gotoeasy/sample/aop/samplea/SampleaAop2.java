package top.gotoeasy.sample.aop.samplea;

import java.lang.reflect.Method;

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
    public Object around1(SuperInvoker superInvoker, Method method, Object ... args) {
        log.info("around2-1 ......... 计算结果+1");
        log.info("around2-1 Method......... {}", method);
        return (double)superInvoker.invoke(args) + 1;
    }

    @After(classes = SampleaBean.class)
    public void after(AopContext context, Method method) {
        log.info("after 计算结果 ......... {}", context.getResult());
        log.info("after Method......... {}", method);
    }
}
