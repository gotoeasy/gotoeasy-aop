package top.gotoeasy.sample.aop.samplea;

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
public class SampleaAop1 {

    private static final Log log = LoggerFactory.getLogger(SampleaAop1.class);

    @Before(value = "xxxxxx", classes = LoggerFactory.class)
    public void nouse(AopContext context) {
        log.info("before .........");
    }

    @Before(classes = SampleaBean.class)
    public void before() {
        log.info("before .........");
    }

    @After(classes = LoggerFactory.class)
    public void after(AopContext context) {
        log.info("after .........");
    }

    @Throwing(classes = LoggerFactory.class)
    public void throwing() {
        log.info("throwing .........");
    }

    @Last(classes = LoggerFactory.class)
    public void last(AopContext context) {
        log.info("last .........");
    }

    @Around(classes = LoggerFactory.class, order = 1)
    public Object around1(SuperInvoker superInvoker, Object ... args) {
        log.info("around1 .........");
        return superInvoker.invoke(args);
    }

    @Around(classes = LoggerFactory.class, order = 2)
    public Object around2(Enhance enhance, SuperInvoker superInvoker, Object ... args) {
        log.info("around2 .........");
        return superInvoker.invoke(args);
    }

    @Around(classes = LoggerFactory.class, order = 3)
    public Object around3(Enhance enhance, SuperInvoker superInvoker, Object ... args) {
        log.info("around3 .........");
        return superInvoker.invoke(args);
    }

    @Around(classes = LoggerFactory.class, order = 4)
    public Object around4(Enhance enhance, SuperInvoker superInvoker, Object ... args) {
        log.info("around4 .........");
        return superInvoker.invoke(args);
    }
}
