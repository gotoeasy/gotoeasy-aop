package top.gotoeasy.sample.aop.sample3;

public class Sample3Add {

	private int total = 0;

	public int add(int intVal) {
		total += intVal;
		return total;
	}

	public int getTotal() {
		return total;
	}
}
