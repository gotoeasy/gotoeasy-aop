package top.gotoeasy.framework.aop.test;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.CodeBuilder;

public class Main {

	public static void main(String[] args) {
		CodeBuilder builder = CodeBuilder.get();
		builder.setSuperclass(Test.class);

		AopTestBefore aopTest = new AopTestBefore();
		AopTestAfter aopTestAfter = new AopTestAfter();
		AopTestThrowing aopTestThrowing = new AopTestThrowing();
		AopTestLast aopTestLast = new AopTestLast();
		AopTestAround aopTestAround = new AopTestAround();

		Method[] methods = Test.class.getDeclaredMethods();
		for ( int i = 0; i < methods.length; i++ ) {
			if ( i == 0 ) {
				builder.setAopAround(methods[i], aopTestAround);
				continue;
			}
			builder.setAopBefore(methods[i], aopTest);
			builder.setAopAfter(methods[i], aopTestAfter);
			builder.setAopThrowing(methods[i], aopTestThrowing);
			builder.setAopLast(methods[i], aopTestLast);
		}

		Test test = (Test)builder.build();

		////////////////////
		for ( int i = 0; i < 10000; i++ ) {
			test.hello("xxxxx");
		}

		long ss = System.currentTimeMillis();
		String xxx = null;
		for ( int i = 0; i < 1000 * 10000; i++ ) {
			xxx = test.hello("xxxxx");
		}
		System.err.println("MyAop: " + (System.currentTimeMillis() - ss) + "MS,   " + xxx);
	}

}
