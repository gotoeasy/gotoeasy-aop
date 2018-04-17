package top.gotoeasy.framework.aop.callback;

import java.lang.reflect.Method;

public interface AopLast {

	public void last(Object proxy, Method method, Object ... args);

}
