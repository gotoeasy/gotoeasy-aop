package top.gotoeasy.framework.aop.config;

public class Sample99Bean extends Sample99BaseBean {

    private int total = 0;

    public int add(int intVal) {
        total += intVal;
        return total;
    }

    public int getTotal() {
        return total;
    }

    public int getTotal2(String s1, String s2) {
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

    public int sum(int ... vals) {
        int sum = 0;
        for ( int i : vals ) {
            sum += i;
        }
        return sum;
    }

    public int sum2(int[] vals) {
        int sum = 0;
        for ( int i : vals ) {
            sum += i;
        }
        return sum;
    }
}
