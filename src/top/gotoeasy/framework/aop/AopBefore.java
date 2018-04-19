package top.gotoeasy.framework.aop;

import java.lang.reflect.Method;

/**
 * 前置拦截处理接口
 * @since 2018/04
 * @author 青松
 */
public interface AopBefore {

	/**
	 * 前置拦截处理
	 * @param proxy 代理对象
	 * @param method 被代理方法
	 * @param args 参数
	 */
	public void before(Object proxy, Method method, Object ... args);

}