package top.gotoeasy.framework.aop;

import java.lang.reflect.Method;

/**
 * 异常拦截处理接口
 * @since 2018/04
 * @author 青松
 */
public interface AopThrowing {

	/**
	 * 异常拦截处理
	 * @param proxy 代理对象
	 * @param method 被代理方法
	 * @param throwable 异常
	 * @param args 参数
	 */
	public void throwing(Object proxy, Method method, Throwable throwable, Object ... args);

}
