package top.gotoeasy.sample.aop.sample99;

public class Sample99BeanConstructor3 {

    private int total = 0;

    public Sample99BeanConstructor3(int val, String ... strings) {
    }

    public int add(int intVal) {
        total += intVal;
        return total;
    }

}
