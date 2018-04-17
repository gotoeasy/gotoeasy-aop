package top.gotoeasy.framework.aop.callback;

import java.lang.reflect.Method;

public interface AopThrowing {

	public void throwing(Object proxy, Method method, Exception e, Object ... args);

}
