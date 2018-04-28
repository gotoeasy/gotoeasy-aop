package top.gotoeasy.sample.aop.sample1;

import top.gotoeasy.framework.aop.EnhanceBuilder;

public class Sample1Main {

    public static void main(String[] args) {
        Sample1Aop aop = new Sample1Aop();
        Sample1Add enhance = EnhanceBuilder.get().setSuperclass(Sample1Add.class).matchAop(aop).build();

        enhance.add(1);
        enhance.add(2);
        enhance.add(3);

        System.err.println("Total: " + enhance.getTotal() + ", Count = " + aop.getCount());
    }

}
