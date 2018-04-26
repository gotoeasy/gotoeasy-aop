package top.gotoeasy.sample.aop.sample2;

public class Sample2Add {

    private int total = 0;

    public int add(int intVal) {
        total += intVal;
        return total;
    }

    public int getTotal() {
        return total;
    }
}
