package top.gotoeasy.framework.aop;

import java.lang.reflect.Method;

@FunctionalInterface
public interface SuperInvoker {

	public Object invoke(Method method, Object ... args);

}
