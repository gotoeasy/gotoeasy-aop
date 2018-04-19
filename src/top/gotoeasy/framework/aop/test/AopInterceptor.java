package top.gotoeasy.framework.aop.test;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class AopInterceptor implements MethodInterceptor {

	AopTestBefore aopTestBefore = new AopTestBefore();

	@Override
	public Object intercept(Object target, Method method, Object[] args, MethodProxy proxy) throws Throwable {

//		before_hello(target, method, (String)args[0]);
		Object rs = proxy.invokeSuper(target, args);
//		after_hello(target, method, (String)args[0]);
		return rs;
	}

}
