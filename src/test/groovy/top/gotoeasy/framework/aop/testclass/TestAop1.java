package top.gotoeasy.framework.aop.testclass;

import java.util.Arrays;

import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.After;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;
import top.gotoeasy.framework.aop.annotation.Before;
import top.gotoeasy.framework.aop.annotation.Last;
import top.gotoeasy.framework.aop.annotation.Throwing;
import top.gotoeasy.framework.aop.testpackages.p1.TestBeanP1;
import top.gotoeasy.framework.aop.testpackages.p2.TestBeanP2;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class TestAop1 {

    private static final Log log = LoggerFactory.getLogger(TestAop1.class);

    private int              cnt = 0;

    @Around(classes = {TestBeanP1.class, TestBeanP2.class}, value = "*.hello(*)")
    public Object around(SuperInvoker superInvoker, Object ... args) {
        cnt++;
        log.debug("around 参数：{}", Arrays.asList(args));
        return superInvoker.invoke(args);
    }

    @Before(classes = TestBeanP1.class, value = "*.hello(*)")
    @After(classes = TestBeanP1.class, value = "*.hello(*)")
    @Last(classes = TestBeanP2.class, value = "*.hello(*)")
    @Throwing(classes = TestBeanP1.class, value = "*.hello(*)")
    public void normalAop(Object ... args) {
        cnt++;
        log.debug("normalAop 参数：{}", Arrays.asList(args));
    }

    public int getCnt() {
        return cnt;
    }

}
