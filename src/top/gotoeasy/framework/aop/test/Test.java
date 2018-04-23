package top.gotoeasy.framework.aop.test;

import top.gotoeasy.framework.aop.ZAopBuilder;

public class Test {

	private static int MAX = 10000 * 10000;// Integer.MAX_VALUE;

	public static void main(String[] args) {
		AopTest aop = new AopTest();
		TatrgetCounter enhancer = (TatrgetCounter)ZAopBuilder.get().setSuperclass(TatrgetCounter.class).matchAop(aop).build();
		enhancer.hello("sdadsas");

		TatrgetCounter sss = new TatrgetCounter();

		//---------------
		long ss = System.currentTimeMillis();
		long total = 0;
		for ( int i = 0; i < MAX; i++ ) {
//			enhancer.hello("sdadsas");
			total = enhancer.add(i);
//			total = sss.add(i);
		}

		System.err.println(total);
		System.err.println((System.currentTimeMillis() - ss) + "MS");

		//---------------
		ss = System.currentTimeMillis();
		for ( int i = 0; i < MAX; i++ ) {
//			enhancer.hello("sdadsas");
//			total = enhancer.add(i);
			total = sss.add(i);
		}

		System.err.println(total);
		System.err.println((System.currentTimeMillis() - ss) + "MS");

	}

}
