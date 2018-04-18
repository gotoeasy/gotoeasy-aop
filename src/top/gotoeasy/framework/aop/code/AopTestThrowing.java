package top.gotoeasy.framework.aop.code;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.callback.AopThrowing;

public class AopTestThrowing implements AopThrowing {

	@Override
	public void throwing(Object proxy, Method method, Throwable throwable, Object ... args) {
		// TODO Auto-generated method stub

	}

}
