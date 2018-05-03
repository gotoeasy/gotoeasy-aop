package top.gotoeasy.sample.aop.sample7;

import top.gotoeasy.framework.aop.EnhanceBuilder;

public class Sample7Main {

    public static void main(String[] args) {
        Sample7AopRole aop = new Sample7AopRole();
        Sample7Bean enhance = EnhanceBuilder.get().setSuperclass(Sample7Bean.class).matchAop(aop).build();

        enhance.hello("world");

    }

}
