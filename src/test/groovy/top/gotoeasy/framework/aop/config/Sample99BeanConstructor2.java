package top.gotoeasy.framework.aop.config;

public class Sample99BeanConstructor2 {

    private int total = 0;

    public Sample99BeanConstructor2() {
    }

    public int add(int intVal) {
        total += intVal;
        return total;
    }

}
