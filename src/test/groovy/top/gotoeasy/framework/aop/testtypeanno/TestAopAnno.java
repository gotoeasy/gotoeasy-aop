package top.gotoeasy.framework.aop.testtypeanno;

import java.lang.reflect.Method;
import java.util.Arrays;

import top.gotoeasy.framework.aop.AopContext;
import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.After;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;
import top.gotoeasy.framework.aop.annotation.Before;
import top.gotoeasy.framework.aop.annotation.Last;
import top.gotoeasy.framework.aop.annotation.Throwing;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class TestAopAnno {

    private static final Log log = LoggerFactory.getLogger(TestAopAnno.class);

    private int              cnt = 0;

    @Around(typeAnnotations = {MyTypeAnno1.class, MyTypeAnno2.class}, value = "*.hello(*)")
    public Object around(SuperInvoker superInvoker, Object ... args) {
        cnt++;
        log.debug("around 参数：{}", Arrays.asList(args));
        return superInvoker.invoke(args);
    }

    @Around(typeAnnotations = {Around.class, Around.class})
    @Around(typeAnnotations = {Around.class, Around.class})
    public Object around2(SuperInvoker superInvoker, Object ... args) {
        cnt++;
        log.debug("around 参数：{}", Arrays.asList(args));
        return superInvoker.invoke(args);
    }

    @Before(typeAnnotations = MyTypeAnno1.class, value = "*.hello(*)")
    @After(typeAnnotations = MyTypeAnno1.class, value = "*.hello(*)")
    @Throwing(typeAnnotations = MyTypeAnno2.class, value = "*.hello(*)")
    @Last(typeAnnotations = MyTypeAnno2.class, value = "*.hello(*)")
    public void normalAop(Enhance enhance, Method method, AopContext context, Exception ex, SuperInvoker superInvoker, Object ... args) {
        cnt++;
        log.debug("normalAop 参数：{}", Arrays.asList(args));
    }

    @Before(typeAnnotations = MyTypeAnno1.class, value = "*.hello(*)")
    @After(typeAnnotations = MyTypeAnno1.class, value = "*.hello(*)")
    @Throwing(typeAnnotations = MyTypeAnno2.class, value = "*.hello(*)")
    @Last(typeAnnotations = MyTypeAnno2.class, value = "*.hello(*)")
    @Before(typeAnnotations = MyTypeAnno1.class, value = "*.hello(*)")
    @After(typeAnnotations = MyTypeAnno1.class, value = "*.hello(*)")
    @Throwing(typeAnnotations = MyTypeAnno2.class, value = "*.hello(*)")
    @Last(typeAnnotations = MyTypeAnno2.class, value = "*.hello(*)")
    public void normalAop2(Enhance enhance, Method method, AopContext context, Exception ex, SuperInvoker superInvoker, Object ... args) {
        cnt++;
        log.debug("normalAop 参数：{}", Arrays.asList(args));
    }

    @After(typeAnnotations = MyTypeAnno2.class, value = "*.hello(*)")
    public void time(Enhance enhance, Method method, AopContext context, Exception ex, SuperInvoker superInvoker, Object ... args) {
        log.info("耗时：{}MS", System.currentTimeMillis() - context.getStartTime());
    }

    public int getCnt() {
        return cnt;
    }

}
