package top.gotoeasy.sample.aop.sample9;

import top.gotoeasy.framework.aop.EnhanceBuilder;

public class Sample99RepeatMain {

    public static void main(String[] args) {
        Sample99RepeatBean bean = EnhanceBuilder.get().setSuperclass(Sample99RepeatBean.class).matchAop(new Sample99RepeatAop()).build();

        bean.sum(1, 2);
        bean.multiply(3, 3);
        bean.add(2, 2);
        bean.hashCode();
        bean.equals(null);
        bean.toString();
    }

}
