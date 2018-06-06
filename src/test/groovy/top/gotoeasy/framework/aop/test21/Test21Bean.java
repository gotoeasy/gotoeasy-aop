package top.gotoeasy.framework.aop.test21;

import java.io.UncheckedIOException;
import java.rmi.UnexpectedException;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class Test21Bean {

    private static final Log log = LoggerFactory.getLogger(Test21Bean.class);

    public Test21Bean(int val) {

    }

    public double compute(int v1, double v2) throws UncheckedIOException, UnexpectedException {
        log.debug("compute({}, {})", v1, v2);
        return v1 / v2;
    }

    public void print(int v1, double v2) throws UncheckedIOException, UnexpectedException {
        log.debug("print({}, {})", v1, v2);
        throw new UnexpectedException("xxxxxxx");
    }
}
