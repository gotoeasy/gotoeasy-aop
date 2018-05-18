package top.gotoeasy.testconfig;

import top.gotoeasy.framework.aop.exception.AopException;

public class Sample99BeanErr {

    public Sample99BeanErr() {
        throw new AopException("test");
    }

    public int sum(Integer ... vals) {
        int sum = 0;
        for ( int i : vals ) {
            sum += i;
        }
        return sum;
    }

}
