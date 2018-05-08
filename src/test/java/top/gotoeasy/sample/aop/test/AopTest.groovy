package top.gotoeasy.sample.aop.test

import static org.junit.Assert.*

import org.junit.Test

import spock.lang.Specification
import top.gotoeasy.framework.aop.exception.AopException
import top.gotoeasy.framework.aop.util.AopUtil
import top.gotoeasy.sample.aop.sample99.Sample99AopAfter
import top.gotoeasy.sample.aop.sample99.Sample99AopAround
import top.gotoeasy.sample.aop.sample99.Sample99AopBefore
import top.gotoeasy.sample.aop.sample99.Sample99AopCheck
import top.gotoeasy.sample.aop.sample99.Sample99AopCheck2
import top.gotoeasy.sample.aop.sample99.Sample99AopCheck3
import top.gotoeasy.sample.aop.sample99.Sample99AopError
import top.gotoeasy.sample.aop.sample99.Sample99AopLast
import top.gotoeasy.sample.aop.sample99.Sample99AopThrowing
import top.gotoeasy.sample.aop.sample99.Sample99Bean
import top.gotoeasy.sample.aop.sample99.Sample99BeanErr


class AopTest extends Specification {

    @Test
    public void "前置拦截"() {

        expect:

        Sample99Bean enhance = EnhanceBuilder.get().setSuperclass(Sample99Bean.class).matchAop(
                new Sample99AopBefore()
                , new Sample99AopAfter()
                , new Sample99AopThrowing()
                , new Sample99AopLast()
                , new Sample99AopAround()
                ).build();

        enhance.add(11) == 11
    }

    @Test
    public void "异常拦截"() {

        expect:

        Sample99AopThrowing aopThrowing = new Sample99AopThrowing();
        Sample99Bean enhance = EnhanceBuilder.get().setSuperclass(Sample99Bean.class).matchAop(aopThrowing).build();

        when:
        enhance.mod(0)

        then:
        thrown(Exception)
        aopThrowing.getException() != null
    }

    @Test
    public void "final class异常"() {

        expect:

        when:
        EnhanceBuilder.get().setSuperclass(String.class).build();

        then:
        Exception ex=  thrown(Exception)
        ex.toString() == "java.lang.UnsupportedOperationException: 无法通过继承来增强的final类：java.lang.String"
    }

    @Test
    public void "环绕拦截独占检查"() {

        expect:

        when:
        EnhanceBuilder.get().setSuperclass(Sample99Bean.class).matchAop(new Sample99AopCheck()).build();

        then:
        Exception ex =  thrown(Exception)
        ex.getClass() == AopException.class

        when:
        EnhanceBuilder.get().setSuperclass(Sample99Bean.class).matchAop(new Sample99AopCheck2()).build();

        then:
        Exception ex2 =  thrown(Exception)
        ex2.getClass() == AopException.class

        when:
        EnhanceBuilder.get().setSuperclass(Sample99Bean.class).matchAop(new Sample99AopCheck3()).build();

        then:
        Exception   ex3 =  thrown(Exception) // 方法返回类型检查
        ex3.getClass() == AopException.class
    }

    @Test
    public void "创建增强对象失败"() {

        expect:

        Sample99AopError  aop = new Sample99AopError();
        when:
        EnhanceBuilder.get().setSuperclass(Sample99BeanErr.class).matchAop(aop).build();

        then:
        Exception ex =  thrown(Exception)
        ex.getClass() == AopException.class
    }


    @Test
    public void "AopUtil静态查找方法异常"() {

        expect:

        when:
        AopUtil.getMethod(null,null,null);

        then:
        Exception ex2 =  thrown(Exception)
        ex2.getClass() == AopException.class
    }
}
