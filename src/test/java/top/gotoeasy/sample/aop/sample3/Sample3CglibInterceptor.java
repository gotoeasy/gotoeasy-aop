package top.gotoeasy.sample.aop.sample3;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class Sample3CglibInterceptor implements MethodInterceptor {

	@Override
	public Object intercept(Object target, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		return (int)proxy.invokeSuper(target, args) + 1;
	}

}
