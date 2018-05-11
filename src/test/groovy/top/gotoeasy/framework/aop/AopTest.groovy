package top.gotoeasy.framework.aop

import static org.junit.Assert.*

import java.lang.reflect.Constructor

import org.junit.Test

import spock.lang.Specification
import top.gotoeasy.framework.aop.config.Sample99AopAfter
import top.gotoeasy.framework.aop.config.Sample99AopAround
import top.gotoeasy.framework.aop.config.Sample99AopBefore
import top.gotoeasy.framework.aop.config.Sample99AopCheck
import top.gotoeasy.framework.aop.config.Sample99AopCheck2
import top.gotoeasy.framework.aop.config.Sample99AopCheck3
import top.gotoeasy.framework.aop.config.Sample99AopError
import top.gotoeasy.framework.aop.config.Sample99AopLast
import top.gotoeasy.framework.aop.config.Sample99AopThrowing
import top.gotoeasy.framework.aop.config.Sample99Bean
import top.gotoeasy.framework.aop.config.Sample99BeanConstructor
import top.gotoeasy.framework.aop.config.Sample99BeanConstructor2
import top.gotoeasy.framework.aop.config.Sample99BeanConstructor3
import top.gotoeasy.framework.aop.config.Sample99BeanConstructor4
import top.gotoeasy.framework.aop.config.Sample99BeanConstructor5
import top.gotoeasy.framework.aop.config.Sample99BeanConstructor6
import top.gotoeasy.framework.aop.config.Sample99BeanErr
import top.gotoeasy.framework.aop.exception.AopException
import top.gotoeasy.framework.aop.util.AopUtil


class AopTest extends Specification {

    @Test
    public void "指定构造方法做增强处理"() {

        expect:
        Constructor<?> constructor = Sample99BeanConstructor.class.getConstructors()[0]

        when:
        // 有增强，指定构造方法
        def obj = EnhanceBuilder.get().setSuperclass(Sample99BeanConstructor.class)
                .setConstructorArgs(constructor, new Sample99Bean(), new Sample99Bean()).matchAop( new Sample99AopLast()).build();

        then:
        Enhance.class.isAssignableFrom(obj.getClass()) == true

        when:
        // 无增强，指定构造方法
        def obj2 = EnhanceBuilder.get().setSuperclass(Sample99BeanConstructor.class)
                .setConstructorArgs(constructor, new Sample99Bean(), new Sample99Bean()).build();

        then:
        Enhance.class.isAssignableFrom(obj2.getClass()) == false


        when:
        // 无增强，指定构造方法
        def obj3 = EnhanceBuilder.get().setSuperclass(Sample99BeanConstructor.class)
                .setConstructorArgs(constructor, new Sample99Bean(), new Object()).build();

        then:
        Exception ex2 =  thrown(Exception)
        ex2.getClass() == AopException.class


        when:
        // 有增强，指定无参构造方法
        obj = EnhanceBuilder.get().setSuperclass(Sample99BeanConstructor2.class)
                .setConstructorArgs(Sample99BeanConstructor2.class.getConstructors()[0])
                .matchAop( new Sample99AopLast()).build();

        then:
        Enhance.class.isAssignableFrom(obj.getClass()) == true
    }


    @Test
    public void "有增强，指定含可变参数构造方法"() {

        expect:

        when:
        // 有增强，指定可变参数构造方法
        String [] args =  new String[2];
        args[0]= "1";
        args[1]= "2";
        def obj = EnhanceBuilder.get().setSuperclass(Sample99BeanConstructor3.class)
                .setConstructorArgs(Sample99BeanConstructor3.class.getConstructors()[0], 4, args)
                .matchAop( new Sample99AopLast()).build();

        then:
        Enhance.class.isAssignableFrom(obj.getClass()) == true

    }

    @Test
    public void "有增强，指定单一变参数构造方法"() {

        expect:

        when:
        // 有增强，指定可变参数构造方法
        String [] args =  new String[2];
        args[0]= "1";
        args[1]= "2";
        def obj = EnhanceBuilder.get().setSuperclass(Sample99BeanConstructor4.class)
                .setConstructorArgs(Sample99BeanConstructor4.class.getConstructors()[0],  args)
                .matchAop( new Sample99AopLast()).build();

        then:
        Enhance.class.isAssignableFrom(obj.getClass()) == true

    }

    @Test
    public void "有增强，指定含数组参数构造方法"() {

        expect:

        when:
        // 有增强，指定数组参数构造方法
        String [] args =  new String[2];
        args[0]= "1";
        args[1]= "2";
        def obj = EnhanceBuilder.get().setSuperclass(Sample99BeanConstructor5.class)
                .setConstructorArgs(Sample99BeanConstructor5.class.getConstructors()[0], args, 1)
                .matchAop( new Sample99AopLast()).build();

        then:
        Enhance.class.isAssignableFrom(obj.getClass()) == true

    }

    @Test
    public void "有增强，指定单一数组参数构造方法"() {

        expect:

        when:
        // 有增强，指定数组参数构造方法
        String [] args =  new String[2];
        args[0]= "1";
        args[1]= "2";
        def obj = EnhanceBuilder.get().setSuperclass(Sample99BeanConstructor6.class)
                .setConstructorArgs(Sample99BeanConstructor6.class.getConstructors()[0], args)
                .matchAop( new Sample99AopLast()).build();

        then:
        Enhance.class.isAssignableFrom(obj.getClass()) == true

    }

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


    @Test
    public void "没有匹配的拦截时不做增强处理"() {

        expect:
        Sample99AopError  aop = new Sample99AopError();

        when:
        def obj = EnhanceBuilder.get().setSuperclass(HashMap.class).matchAopList(Arrays.asList(aop)).build();

        then:
        obj.getClass() == HashMap.class


        when:
        EnhanceBuilder.get().setSuperclass(Runnable.class).matchAop(aop).build();

        then:
        Exception ex2 =  thrown(Exception)
        ex2.getClass() == AopException.class
    }

    @Test
    public void "AopUtil私有构造方法调用"() {

        expect:
        Constructor<?> constructor = AopUtil.class.getDeclaredConstructor()
        constructor.setAccessible(true)

        when:
        constructor.newInstance()

        then:
        notThrown(Exception)
    }
}
