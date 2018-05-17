package top.gotoeasy.framework.aop.config;

import top.gotoeasy.framework.aop.exception.AopException;

public class Sample99BeanErr {

    public Sample99BeanErr() {
        throw new AopException("test");
    }

    public int sum(int ... vals) {
        int sum = 0;
        for ( int i : vals ) {
            sum += i;
        }
        return sum;
    }

}