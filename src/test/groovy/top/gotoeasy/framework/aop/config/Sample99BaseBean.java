package top.gotoeasy.framework.aop.config;

public class Sample99BaseBean {

    public String hello(String name) {
        return "Hello " + name;
    }

    public int add(String val1, String val2) {
        return Integer.valueOf(val1) + Integer.valueOf(val2);
    }

    @Deprecated
    public void init() {
    }

    @Deprecated
    public void arount(String val) {
    }

    public int count(String val1, String val2) {
        return Integer.valueOf(val1) + Integer.valueOf(val2);
    }

    public int count2(String val) {
        return 0;
    }

    public void arountVoid(String val) {

    }
}
