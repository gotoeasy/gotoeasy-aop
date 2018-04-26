package top.gotoeasy.sample.aop.sample1;

public class Sample1Add {

	private int total = 0;

	public int add(int intVal) {
		total += intVal;
		return total;
	}

	public int getTotal() {
		return total;
	}
}
