package top.gotoeasy.sample.aop.sample99;

public class Sample99BeanConstructor {

    private Sample99Bean sample99Bean;
    private int          total = 0;

    public Sample99BeanConstructor(Sample99Bean sample99Bean, Sample99Bean sample99Bean2) {
        this.sample99Bean = sample99Bean;
    }

    public int add(int intVal) {
        total += intVal;
        return total;
    }

    public Sample99Bean getSample99Bean() {
        return sample99Bean;
    }

}
