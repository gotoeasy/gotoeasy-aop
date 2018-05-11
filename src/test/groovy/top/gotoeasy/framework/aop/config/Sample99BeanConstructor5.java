package top.gotoeasy.framework.aop.config;

public class Sample99BeanConstructor5 {

    private int total = 0;

    public Sample99BeanConstructor5(String[] strings, int val) {
    }

    public int add(int intVal) {
        total += intVal;
        return total;
    }

}
