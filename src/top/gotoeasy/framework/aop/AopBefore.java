package top.gotoeasy.framework.aop;

import java.lang.reflect.Method;

public interface AopBefore {

	public void before(Object proxy, Method method, Object ... args);

}
