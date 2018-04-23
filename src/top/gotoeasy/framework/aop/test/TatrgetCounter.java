package top.gotoeasy.framework.aop.test;

public class TatrgetCounter {

	private int total = 0;

	public int add(int i) {
		total = i;
		return total;
	}

	public String hello(String name) {
//		System.err.println("  TatrgetCounter");
		return name;
	}
}
