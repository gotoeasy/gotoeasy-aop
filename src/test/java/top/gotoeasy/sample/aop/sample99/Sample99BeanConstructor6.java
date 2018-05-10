package top.gotoeasy.sample.aop.sample99;

public class Sample99BeanConstructor6 {

    private int total = 0;

    public Sample99BeanConstructor6(String[] strings) {
    }

    public int add(int intVal) {
        total += intVal;
        return total;
    }

}
