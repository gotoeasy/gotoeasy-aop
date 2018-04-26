package top.gotoeasy.sample.aop.sample3;

import net.sf.cglib.proxy.Enhancer;
import top.gotoeasy.framework.aop.EnhanceBuilder;

public class Sample3Main {

    private static int MAX = 10000 * 10000;

    public static void main(String[] args) {
        gotoeasyAop();
        noAop();
        cglibAop();
    }

    private static void noAop() {
        Sample3Add sample = new Sample3Add();

        for ( int i = 0; i < 10000; i++ ) {
            sample.add(0);
        }

        System.err.println("[no aop]就绪, Total = " + sample.getTotal());

        long mark = System.currentTimeMillis();
        for ( int i = 0; i < MAX; i++ ) {
            sample.add(1);
        }

        System.err.println("[no aop]耗时  " + (System.currentTimeMillis() - mark) + " MS, Total = " + sample.getTotal());
    }

    private static void gotoeasyAop() {
        Sample3Aop aop = new Sample3Aop();
        Sample3Add sample = (Sample3Add)EnhanceBuilder.get().setSuperclass(Sample3Add.class).matchAop(aop).build();

        for ( int i = 0; i < 10000; i++ ) {
            sample.add(0);
        }

        System.err.println("[gotoeasy aop]就绪, Total = " + sample.getTotal());

        long mark = System.currentTimeMillis();
        for ( int i = 0; i < MAX; i++ ) {
            sample.add(1);
        }

        System.err.println("[gotoeasy aop]耗时  " + (System.currentTimeMillis() - mark) + " MS, Total = " + sample.getTotal());
    }

    private static void cglibAop() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Sample3Add.class);
        enhancer.setCallback(new Sample3CglibInterceptor());
        Sample3Add sample = (Sample3Add)enhancer.create();

        for ( int i = 0; i < 10000; i++ ) {
            sample.add(0);
        }

        System.err.println("[cglib aop]就绪, Total = " + sample.getTotal());
        long mark = System.currentTimeMillis();
        for ( int i = 0; i < MAX; i++ ) {
            sample.add(1);
        }
        System.err.println("[cglib aop]耗时  " + (System.currentTimeMillis() - mark) + " MS, Total = " + sample.getTotal());
    }
}
