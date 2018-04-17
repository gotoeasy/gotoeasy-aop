package top.gotoeasy.framework.aop.callback;

import java.lang.reflect.Method;

public interface AopAround {

	public Object around(Object proxy, Method method, Object ... args);

}
