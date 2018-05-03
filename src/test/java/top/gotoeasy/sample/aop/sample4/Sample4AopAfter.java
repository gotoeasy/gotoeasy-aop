package top.gotoeasy.sample.aop.sample4;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.annotation.After;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class Sample4AopAfter {

    private static final Log log = LoggerFactory.getLogger(Sample4AopAfter.class);

    private int              count;

    @After("*.Sample4Bean.*")
    public void after(Enhance enhance, Method method, Object ... args) {
        log.info(" @After  拦截{}", method.getName());
        count++;
    }

    public int getCount() {
        return count;
    }

}
