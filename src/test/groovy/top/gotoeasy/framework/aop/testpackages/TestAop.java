package top.gotoeasy.framework.aop.testpackages;

import java.util.Arrays;

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
public class TestAop {

    private static final Log log = LoggerFactory.getLogger(TestAop.class);

    private int              cnt = 0;

    @Around(packages = "top.gotoeasy.framework.aop.testpackages.p1,top.gotoeasy.framework.aop.testpackages.p2", value = "*.hello(*)")
    public Object around(SuperInvoker superInvoker, Object ... args) {
        cnt++;
        log.debug("around 参数：{}", Arrays.asList(args));
        return superInvoker.invoke(args);
    }

    @Before(packages = "top.gotoeasy.framework.aop.testpackages.p1", value = "*.hello(*)")
    @After(packages = "top.gotoeasy.framework.aop.testpackages.p1", value = "*.hello(*)")
    @Last(packages = "top.gotoeasy.framework.aop.testpackages.p1", value = "*.hello(*)")
    @Throwing(packages = "top.gotoeasy.framework.aop.testpackages.p1", value = "*.hello(*)")
    public void normalAop(Object ... args) {
        cnt++;
        log.debug("normalAop 参数：{}", Arrays.asList(args));
    }

    public int getCnt() {
        return cnt;
    }

}
