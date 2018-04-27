package top.gotoeasy.sample.aop.sample4;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class Sample4AopAround {

    private static final Log log = LoggerFactory.getLogger(Sample4AopThrowing.class);

    @Around(annotation = Deprecated.class, matchDeclaredMethod = true)
    public void around(Enhance enhance, Method method, Object ... args) {
        log.info("@Around 拦截{}", method.getName());
    }

}
