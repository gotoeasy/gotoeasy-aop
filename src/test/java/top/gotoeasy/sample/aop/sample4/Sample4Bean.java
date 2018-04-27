package top.gotoeasy.sample.aop.sample4;

public class Sample4Bean extends Sample4BaseBean {

    private int total = 0;

    public int add(int intVal) {
        total += intVal;
        return total;
    }

    public int getTotal() {
        return total;
    }

    public boolean isReady() {
        return total == 0;
    }

    public final void initTotal() {
        total = 0;
    }

    public int addAll(int ... intVals) {
        for ( int val : intVals ) {
            total += val;
        }
        return total;
    }

    public int mod(int val) {
        return total % val;
    }

}
