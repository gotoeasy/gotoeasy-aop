package top.gotoeasy.framework.aop.code;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.callback.AopAfter;
import top.gotoeasy.framework.aop.callback.AopBefore;

public class AopTest implements AopBefore, AopAfter {

	@Override
	public void before(Object proxy, Method method, Object ... args) {
//		System.err.println(args);
	}

	@Override
	public void after(Object proxy, Method method, Object ... args) {
		// TODO Auto-generated method stub

	}

}
