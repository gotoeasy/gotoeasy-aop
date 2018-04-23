package top.gotoeasy.framework.aop.test;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.Enhancer;
import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.Around;

public class AopTest {

	@Around("*.hello(*)")
	public String aroundHello2(Enhancer enhancer, Method method, SuperInvoker superInvoker, Object ... args) {
//		System.err.println("  AopTest");
		return (String)superInvoker.invoke(method, args);
	}

	@Around("*.add(*)")
	public int add(Enhancer enhancer, Method method, SuperInvoker superInvoker, int p) {
//		System.err.println("  AopTest");
		return (int)superInvoker.invoke(method, p);
	}

}
