package top.gotoeasy.framework.aop;

import java.lang.reflect.Method;

public interface AopThrowing {

	public void throwing(Object proxy, Method method, Throwable throwable, Object ... args);

}
