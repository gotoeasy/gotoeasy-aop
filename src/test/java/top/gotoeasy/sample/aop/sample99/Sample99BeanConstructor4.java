package top.gotoeasy.sample.aop.sample99;

public class Sample99BeanConstructor4 {

    private int total = 0;

    public Sample99BeanConstructor4(String ... strings) {
    }

    public int add(int intVal) {
        total += intVal;
        return total;
    }

}
