package top.gotoeasy.sample.aop.sample7;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Before;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class Sample7AopRole {

    private static final Log log = LoggerFactory.getLogger(Sample7AopRole.class);

    @Before("*.Sample7Bean.*")
    public void before() {
        log.debug("简单的权限检查，不需要参数也可以");
    }

    @Before("*.Sample7Bean.*")
    public void before(Method method, Object ... args) {
        log.debug("权限检查，也可以接收参数进行判断");
    }

}
