package top.gotoeasy.framework.aop;

import java.lang.reflect.Method;

public interface AopAfter {

	public void after(Object proxy, Method method, Object ... args);

}
