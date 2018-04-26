package top.gotoeasy.sample.aop.sample3;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class Sample3CglibInterceptor implements MethodInterceptor {

	private int count = 0;

	@Override
	public Object intercept(Object target, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		count++;
		return proxy.invokeSuper(target, args);
	}

	public int getCount() {
		return count;
	}

}
