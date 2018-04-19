package top.gotoeasy.framework.aop.test;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.AopBefore;

public class AopTestBefore implements AopBefore {

	@Override
	public void before(Object proxy, Method method, Object ... args) {
		System.err.println(args);
	}

}
