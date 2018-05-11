package top.gotoeasy.framework.aop.config;

public class Sample99BeanConstructor6 {

    private int total = 0;

    public Sample99BeanConstructor6(String[] strings) {
    }

    public int add(int intVal) {
        total += intVal;
        return total;
    }

}
