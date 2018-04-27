package top.gotoeasy.sample.aop.sample4;

public class Sample4BaseBean {

    public String hello(String name) {
        return "Hello " + name;
    }

    @Deprecated
    public void init() {
    }
}
