package top.gotoeasy.framework.aop.code;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.callback.AopThrowing;

public class AopTestThrowing implements AopThrowing {

	@Override
	public void throwing(Object proxy, Method method, Exception e, Object ... args) {
		// TODO Auto-generated method stub

	}

}
