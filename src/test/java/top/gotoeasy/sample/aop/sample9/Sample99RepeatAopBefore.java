package top.gotoeasy.sample.aop.sample9;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Before;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class Sample99RepeatAopBefore {

    private static final Log log = LoggerFactory.getLogger(Sample99RepeatAopBefore.class);

//    @Before("*.Sample99RepeatBean.sum(*)")
//    @Before("*.Sample99RepeatBean.multiply(*)")
//    public void before1(Method method, int v1, int v2) {
//        log.info("before1调用 {}({}, {})", method.getName(), v1, v2);
//    }
//
//    @Before(value = "*.Sample99RepeatBean.sum(*)", matchSuperMethod = true, matchToString = true)
//    @Before(value = "*.Object.*", matchSuperMethod = true, matchEquals = true)
//    @Before(value = "*.Object.*", matchSuperMethod = true, matchToString = true)
//    public void before2(Method method) {
//        log.info("before2调用 {}", method.getName());
//    }
//
//    @Before(value = "*.Sample99RepeatBean.sum(*)")
//    public void before3(Method method) {
//        log.info("before3调用 {}", method.getName());
//    }

    @Before(annotations = Deprecated.class)
    public void before4(Method method) {
        log.warn("正在调用不推荐的方法:{}", method);
    }
}
