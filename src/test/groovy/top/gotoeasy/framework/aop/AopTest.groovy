package top.gotoeasy.framework.aop

import static org.junit.Assert.*

import java.lang.reflect.Constructor

import org.junit.Test

import spock.lang.Specification
import top.gotoeasy.framework.aop.annotation.Around
import top.gotoeasy.framework.aop.exception.AopException
import top.gotoeasy.framework.aop.test18.Test18Aop
import top.gotoeasy.framework.aop.test18.Test18Bean
import top.gotoeasy.framework.aop.test19.Test19Aop
import top.gotoeasy.framework.aop.test19.Test19Bean
import top.gotoeasy.framework.aop.test20.Test20Aop
import top.gotoeasy.framework.aop.test20.Test20Bean
import top.gotoeasy.framework.aop.testclass.TestAop1
import top.gotoeasy.framework.aop.testconfig.Sample99AopAfter
import top.gotoeasy.framework.aop.testconfig.Sample99AopAround
import top.gotoeasy.framework.aop.testconfig.Sample99AopBefore
import top.gotoeasy.framework.aop.testconfig.Sample99AopCheck
import top.gotoeasy.framework.aop.testconfig.Sample99AopCheck2
import top.gotoeasy.framework.aop.testconfig.Sample99AopCheck3
import top.gotoeasy.framework.aop.testconfig.Sample99AopError
import top.gotoeasy.framework.aop.testconfig.Sample99AopLast
import top.gotoeasy.framework.aop.testconfig.Sample99AopThrowing
import top.gotoeasy.framework.aop.testconfig.Sample99Bean
import top.gotoeasy.framework.aop.testconfig.Sample99BeanConstructor
import top.gotoeasy.framework.aop.testconfig.Sample99BeanConstructor2
import top.gotoeasy.framework.aop.testconfig.Sample99BeanConstructor3
import top.gotoeasy.framework.aop.testconfig.Sample99BeanConstructor4
import top.gotoeasy.framework.aop.testconfig.Sample99BeanConstructor5
import top.gotoeasy.framework.aop.testconfig.Sample99BeanConstructor6
import top.gotoeasy.framework.aop.testconfig.Sample99BeanErr
import top.gotoeasy.framework.aop.testmethod.MethodAopAround
import top.gotoeasy.framework.aop.testmethod.MethodDesc
import top.gotoeasy.framework.aop.testpackages.TestAop
import top.gotoeasy.framework.aop.testpackages.p1.TestBeanP1
import top.gotoeasy.framework.aop.testpackages.p2.TestBeanP2
import top.gotoeasy.framework.aop.testtypeanno.TestAopAnno
import top.gotoeasy.framework.aop.testtypeanno.TestBeanAnno1
import top.gotoeasy.framework.aop.testtypeanno.TestBeanAnno2
import top.gotoeasy.framework.aop.util.AopUtil

class AopTest extends Specification {

    @Test
    public void "01指定构造方法做增强处理"() {

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
    public void "02有增强，指定含可变参数构造方法"() {

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
    public void "03有增强，指定单一变参数构造方法"() {

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
    public void "04有增强，指定含数组参数构造方法"() {

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
    public void "05有增强，指定单一数组参数构造方法"() {

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
    public void "06前置拦截"() {

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
    public void "07异常拦截"() {

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
    public void "08final class没有异常"() {

        expect:

        when:
        EnhanceBuilder.get().setSuperclass(String.class).build();

        then:
        noExceptionThrown()
    }

    @Test
    public void "09环绕拦截可以重复"() {

        expect:

        when:
        EnhanceBuilder.get().setSuperclass(Sample99Bean.class).matchAop(new Sample99AopCheck()).build();

        then:
        noExceptionThrown()

        when:
        EnhanceBuilder.get().setSuperclass(Sample99Bean.class).matchAop(new Sample99AopCheck2()).build();

        then:
        noExceptionThrown()

        when:
        EnhanceBuilder.get().setSuperclass(Sample99Bean.class).matchAop(new Sample99AopCheck3()).build();

        then:
        Exception   ex3 =  thrown(Exception) // 方法返回类型检查
        ex3.getClass() == AopException.class
    }

    @Test
    public void "10创建增强对象失败"() {

        expect:

        Sample99AopError  aop = new Sample99AopError();
        when:
        EnhanceBuilder.get().setSuperclass(Sample99BeanErr.class).matchAop(aop).build();

        then:
        thrown(Exception)
    }


    @Test
    public void "11AopUtil静态查找方法异常"() {

        expect:

        when:
        AopUtil.getMethod(null,null,null);

        then:
        Exception ex2 =  thrown(Exception)
        ex2.getClass() == AopException.class
    }


    @Test
    public void "12没有匹配的拦截时不做增强处理"() {

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
    public void "13AopUtil私有构造方法调用"() {

        expect:
        Constructor<?> constructor = AopUtil.class.getDeclaredConstructor()
        constructor.setAccessible(true)

        when:
        constructor.newInstance()

        then:
        notThrown(Exception)
    }


    @Test
    public void "14各种方法参数类型测试"() {

        expect:
        MethodDesc obj = EnhanceBuilder.get().setSuperclass(MethodDesc.class).matchAop( new MethodAopAround()).build();

        obj.add(1, 2) == 3
        obj.sum(1, 2, 3) == 6
        obj.sum("sum", 1, 2, 3) == "sum=6"
        obj.sum2("sum", 1, 2, 3) == "sum=6"
        obj.hello("1") == "Hello 1"
        String[] arrStr = ['a', 'b', 'c']
        obj.join(arrStr) == "abc"
        obj.join("join=", arrStr) == "join=abc"
        String[][] strss = [['a', 'b', 'c'], ['d', 'e', 'f']]
        obj.writeLog(strss) == 2
        Class<?>[] classes = [EnhanceBuilder.class, MethodDesc.class]
        obj.testParam(classes) == true
        obj.testParam2(classes) == true
        obj.testParam3("test", classes) == true
        obj.writeLog(Around.class, 1, "a")
        obj.writeLog(new HashMap(), Around.class)

        obj.sumIntVars(1, 2, 3) == 6
        obj.sumLongAry(1, 2, 3, 4) == 10
        int[][] intss = [[1, 2, 3], [4, 5, 6]]
        obj.sumIntAry2(intss) == 21
    }

    @Test
    public void "15测试指定包名范围"() {

        expect:
        def testAop = new TestAop();
        TestBeanP1 obj1 = EnhanceBuilder.get().setSuperclass(TestBeanP1.class).matchAop( testAop).build();
        TestBeanP2 obj2 = EnhanceBuilder.get().setSuperclass(TestBeanP2.class).matchAop( testAop).build();

        obj1.hello("aop")
        obj2.hello("aop")

        testAop.getCnt() == 5
    }


    @Test
    public void "16测试指定类名范围"() {

        expect:
        def testAop = new TestAop1();
        TestBeanP1 obj1 = EnhanceBuilder.get().setSuperclass(TestBeanP1.class).matchAop( testAop).build();
        TestBeanP2 obj2 = EnhanceBuilder.get().setSuperclass(TestBeanP2.class).matchAop( testAop).build();

        obj1.hello("aop")
        obj2.hello("aop")

        testAop.getCnt() == 5
    }

    @Test
    public void "17测试指定类注解范围"() {

        expect:
        def testAop = new TestAopAnno();
        TestBeanAnno1 obj1 = EnhanceBuilder.get().setSuperclass(TestBeanAnno1.class).matchAop( testAop).build();
        TestBeanAnno2 obj2 = EnhanceBuilder.get().setSuperclass(TestBeanAnno2.class).matchAop( testAop).build();

        obj1.hello("aop")
        obj2.hello("aop")
        obj2.hello("aop", 1)

        testAop.getCnt() == 11
    }

    @Test
    public void "18 TestNoAop"() {

        expect:
        def testAop = new Test18Aop();
        Test18Bean obj = EnhanceBuilder.get().setSuperclass(Test18Bean.class).matchAop(testAop).build();

        obj.getClass() != Test18Bean.class
    }

    @Test
    public void "19 没有After，但有Throwing或Last要用到context"() {

        expect:
        //  DefaultConfig.getInstance().set("log.level.trace", "true");
        def testAop = new Test19Aop();
        Test19Bean obj = EnhanceBuilder.get().setSuperclass(Test19Bean.class).matchAop(testAop).build();

        when:
        obj.compute(1, 0)
        then:
        thrown(Exception)
    }


    @Test
    public void "20 拦截异常声明的方法"() {

        expect:
        def aop = new Test20Aop();
        Test20Bean obj = EnhanceBuilder.get().setSuperclass(Test20Bean.class).matchAop(aop).build();

        obj.compute(9, 2.0) == 4.5

        when:
        obj.print(1, 2.0)
        then:
        thrown(RuntimeException)
    }
}
