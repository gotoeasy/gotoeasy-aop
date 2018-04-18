package top.gotoeasy.framework.aop.code;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.callback.AopBefore;

public class AopTestBefore implements AopBefore {

	@Override
	public void before(Object proxy, Method method, Object ... args) {
//		System.err.println(args);
	}

}
