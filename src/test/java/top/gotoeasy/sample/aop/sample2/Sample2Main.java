package top.gotoeasy.sample.aop.sample2;

import net.sf.cglib.proxy.Enhancer;
import top.gotoeasy.framework.aop.EnhanceBuilder;

public class Sample2Main {

    private static int MAX = 10000 * 10000;

    public static void main(String[] args) {
        gotoeasyAop();
        noAop();
        cglibAop();
    }

    private static void noAop() {
        Sample2Add sample = new Sample2Add();

        for ( int i = 0; i < 10000; i++ ) {
            sample.add(0);
        }

        System.err.println("[no aop]就绪, Total = " + sample.getTotal());

        int total = 0;
        long mark = System.currentTimeMillis();
        for ( int i = 0; i < MAX; i++ ) {
            total = sample.add(1) + i % 1;
        }

        System.err.println("[no aop]耗时  " + (System.currentTimeMillis() - mark) + " MS, Total = " + total);
    }

    private static void gotoeasyAop() {
        Sample2Aop aop = new Sample2Aop();
        Sample2Add sample = EnhanceBuilder.get().setSuperclass(Sample2Add.class).matchAop(aop).build();

        for ( int i = 0; i < 10000; i++ ) {
            sample.add(0);
        }

        System.err.println("[gotoeasy aop]就绪, Total = " + sample.getTotal());

        int total = 0;
        long mark = System.currentTimeMillis();
        for ( int i = 0; i < MAX; i++ ) {
            total = sample.add(1);
        }

        System.err.println("[gotoeasy aop]耗时  " + (System.currentTimeMillis() - mark) + " MS, Total = " + total);
    }

    private static void cglibAop() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Sample2Add.class);
        enhancer.setCallback(new Sample2CglibInterceptor());
        Sample2Add sample = (Sample2Add)enhancer.create();

        for ( int i = 0; i < 10000; i++ ) {
            sample.add(0);
        }
        sample.add(-1);

        System.err.println("[cglib aop]就绪, Total = " + sample.getTotal());
        int total = 0;
        long mark = System.currentTimeMillis();
        for ( int i = 0; i < MAX; i++ ) {
            total = sample.add(1);
        }
        System.err.println("[cglib aop]耗时  " + (System.currentTimeMillis() - mark) + " MS, Total = " + total);
    }
}
